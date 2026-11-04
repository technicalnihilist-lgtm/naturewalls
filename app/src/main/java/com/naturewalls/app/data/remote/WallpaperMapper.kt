package com.naturewalls.app.data.remote

import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.util.HtmlText
import java.util.Locale

/**
 * Turns a raw Commons `page` object into a [Wallpaper], and is the single place where the
 * licence and attribution rules are enforced.
 *
 * A page is dropped (returns null) when:
 *  - it has no imageinfo, no original URL, or is not a JPEG/PNG photograph
 *  - the original is narrower than 1200 px (too soft for a modern phone screen)
 *  - the licence is missing or outside the allow-list (see [LicensePolicy])
 *  - attribution is required but no creator is published
 *  - its Commons categories mark it as an artwork, diagram, animal portrait, building, etc.
 */
object WallpaperMapper {

    const val MIN_WIDTH = 1200
    private const val THUMB_WIDTH = 600

    private val ALLOWED_MIME = setOf("image/jpeg", "image/png", "image/jpg")
    private val PHOTO_EXTENSION = Regex("\\.(jpe?g|png)$", RegexOption.IGNORE_CASE)

    /**
     * Categories that mean "this is not a nature photograph". Commons search is generous, so this
     * keeps paintings, prints, birds, buildings and vehicles out of a wallpaper feed.
     */
    private val BLOCKED_CATEGORY_HINTS = listOf(
        "painting", "artwork", "drawing", "engraving", "etching", "lithograph", "woodcut",
        "print", "sculpture", "map", "diagram", "illustration", "poster", "stamp", "coin",
        "manuscript", "book", "document", "logo", "coat of arms", "screenshot", "svg",
        "bird", "aves", "insect", "mammal", "reptile", "amphibian", "fish", "arachnid",
        "mollusc", "animal", "people", "person", "portrait", "human",
        "building", "church", "cathedral", "castle", "temple", "mosque", "tower", "bridge",
        "automobile", "train", "railway", "ship", "aircraft", "vehicle", "machine",
        "sport", "event", "flag", "text", "sign", "interior", "furniture", "food"
    )

    fun map(
        page: CommonsPage,
        groupId: String? = null,
        subCategoryId: String? = null
    ): Wallpaper? {
        val pageId = page.pageId ?: return null
        val rawTitle = page.title ?: return null
        val info = page.imageInfo?.firstOrNull() ?: return null

        val fullUrl = info.url ?: return null
        val sourcePageUrl = info.descriptionUrl ?: info.descriptionShortUrl ?: return null
        val width = info.width ?: 0
        val height = info.height ?: 0
        if (width < MIN_WIDTH || height <= 0) return null

        val mime = info.mime?.lowercase(Locale.ROOT).orEmpty()
        if (mime.isNotEmpty() && mime !in ALLOWED_MIME) return null
        if (!PHOTO_EXTENSION.containsMatchIn(rawTitle)) return null

        val md = info.extMetadata.orEmpty()
        val licenseName = md.text("LicenseShortName") ?: return null
        val usageTerms = md.text("UsageTerms")
        val decision = LicensePolicy.evaluate(licenseName, usageTerms)
        if (!decision.allowed) return null

        val creator = md.text("Artist")
        if (decision.requiresAttribution && creator.isNullOrBlank()) return null

        val categories = md.text("Categories")
            ?.split("|")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        if (categories.any { category -> isBlocked(category) }) return null

        val previewUrl = info.thumbUrl ?: fullUrl
        val thumbUrl = downscaleThumb(previewUrl, THUMB_WIDTH, width)

        val licenseUrl = md.text("LicenseUrl")?.takeIf { it.startsWith("http") }

        return Wallpaper(
            id = pageId,
            title = displayTitle(rawTitle),
            thumbUrl = thumbUrl,
            previewUrl = previewUrl,
            fullUrl = fullUrl,
            width = width,
            height = height,
            creator = creator,
            credit = md.text("Credit") ?: md.text("Attribution"),
            licenseName = licenseName,
            licenseUrl = licenseUrl,
            description = HtmlText.shorten(md.text("ImageDescription")),
            modificationNote = modificationNote(decision),
            sourcePageUrl = sourcePageUrl,
            locationLabel = locationLabel(md),
            categories = categories,
            uploadDate = info.timestamp,
            groupId = groupId,
            subCategoryId = subCategoryId
        )
    }

    private fun isBlocked(category: String): Boolean {
        val lower = category.lowercase(Locale.ROOT)
        return BLOCKED_CATEGORY_HINTS.any { it in lower }
    }

    private fun modificationNote(decision: LicensePolicy.Decision): String = buildString {
        append("Unmodified — the app shows Wikimedia's own rendered thumbnail and downloads the ")
        append("file exactly as published.")
        if (decision.requiresShareAlike) {
            append(" ShareAlike: any redistributed copy must keep this licence and credit.")
        }
    }

    /** "File:Foo_bar.jpg" → "Foo bar". */
    private fun displayTitle(rawTitle: String): String {
        val withoutPrefix = rawTitle.substringAfter(':', rawTitle).trim()
        val dot = withoutPrefix.lastIndexOf('.')
        val base = if (dot > 0) withoutPrefix.substring(0, dot) else withoutPrefix
        return base.replace('_', ' ').trim().ifBlank { rawTitle }
    }

    /**
     * Wikimedia serves thumbnails at any width through the same path, e.g.
     * `…/thumb/a/ab/Name.jpg/1600px-Name.jpg`. Rewriting the width segment gives us a light
     * grid thumbnail without a second API round trip.
     */
    private fun downscaleThumb(url: String, targetWidth: Int, originalWidth: Int): String {
        if (originalWidth <= targetWidth) return url
        val rewritten = url.replace(Regex("/\\d+px-"), "/${targetWidth}px-")
        return if (rewritten == url) url else rewritten
    }

    /**
     * Location is shown only when Commons publishes coordinates or an explicit location value for
     * the file. Otherwise the UI falls back to the natural subject — nothing is guessed.
     */
    private fun locationLabel(md: Map<String, ExtMetadataValue>): String? {
        val lat = md.text("GPSLatitude")?.toDoubleOrNull()
        val lon = md.text("GPSLongitude")?.toDoubleOrNull()
        if (lat != null && lon != null) {
            val ns = if (lat >= 0) "N" else "S"
            val ew = if (lon >= 0) "E" else "W"
            return String.format(
                Locale.US,
                "%.3f° %s, %.3f° %s",
                kotlin.math.abs(lat), ns, kotlin.math.abs(lon), ew
            )
        }
        return md.text("Location")
    }

    private fun Map<String, ExtMetadataValue>.text(key: String): String? =
        HtmlText.clean(this[key]?.value)
}
