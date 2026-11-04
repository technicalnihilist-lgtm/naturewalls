package com.naturewalls.app.data.model

/**
 * A nature photograph discovered live on Wikimedia Commons.
 *
 * Every field here is populated from the Commons API response (`imageinfo` + `extmetadata`).
 * Nothing is invented: if the uploader did not publish a value, the field stays null and the
 * UI says so explicitly.
 */
data class Wallpaper(
    val id: Long,
    val title: String,
    val thumbUrl: String,
    val previewUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val creator: String?,
    val credit: String?,
    val licenseName: String,
    val licenseUrl: String?,
    val description: String?,
    val modificationNote: String,
    val sourcePageUrl: String,
    val locationLabel: String?,
    val categories: List<String>,
    val uploadDate: String?,
    val groupId: String? = null,
    val subCategoryId: String? = null
) {
    val resolutionLabel: String
        get() = "$width × $height"

    val megapixels: String
        get() {
            val mp = (width.toLong() * height.toLong()) / 1_000_000.0
            return String.format("%.1f MP", mp)
        }

    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f

    val shortTitle: String
        get() = if (title.length > 70) title.take(67).trimEnd() + "…" else title

    /** Attribution string required by CC BY / CC BY-SA. */
    val attributionLine: String
        get() {
            val who = creator?.takeIf { it.isNotBlank() } ?: "Creator not stated on Commons"
            return "$who · $licenseName · Wikimedia Commons"
        }
}
