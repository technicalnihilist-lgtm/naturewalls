package com.naturewalls.app.data.remote

/**
 * Licence gate for NatureWalls.
 *
 * Only freely reusable photographs are admitted:
 *  - CC0 / Public Domain (no conditions)
 *  - CC BY (attribution)
 *  - CC BY-SA (attribution + ShareAlike)
 *
 * Rejected: anything NonCommercial (cannot be used in a commercial distribution), anything
 * NoDerivatives (a resized/cropped wallpaper is a derivative), "fair use", unknown or missing
 * licence data, and any "all rights reserved" style terms.
 */
object LicensePolicy {

    private val NC = Regex("(^|[\\s\\-])nc([\\s\\-]|$)", RegexOption.IGNORE_CASE)
    private val ND = Regex("(^|[\\s\\-])nd([\\s\\-]|$)", RegexOption.IGNORE_CASE)

    /** Applied to the licence short name only. */
    private val REJECT_LICENSE_HINTS = listOf(
        "noncommercial", "non-commercial", "fair use", "all rights reserved",
        "no derivative", "unknown", "unclear", "permission"
    )

    /** Applied to `UsageTerms` (free text) — narrower, because CC terms mention "copyright". */
    private val REJECT_TERMS_HINTS = listOf(
        "fair use", "all rights reserved", "noncommercial", "non-commercial"
    )

    data class Decision(
        val allowed: Boolean,
        val requiresAttribution: Boolean,
        val requiresShareAlike: Boolean,
        val reason: String? = null
    )

    fun evaluate(licenseShortName: String?, usageTerms: String? = null): Decision {
        val license = licenseShortName?.trim().orEmpty()
        if (license.isEmpty()) {
            return Decision(false, false, false, "No licence published")
        }
        val lower = license.lowercase()
        val terms = usageTerms?.lowercase().orEmpty()

        if (NC.containsMatchIn(lower) || ND.containsMatchIn(lower)) {
            return Decision(false, false, false, "NonCommercial/NoDerivatives licence")
        }
        if (REJECT_LICENSE_HINTS.any { it in lower } || REJECT_TERMS_HINTS.any { it in terms }) {
            return Decision(false, false, false, "Licence not free for reuse")
        }

        val isCc0 = lower.startsWith("cc0") || lower.startsWith("cc-0") || lower.startsWith("cc zero")
        val isPublicDomain = "public domain" in lower || lower.startsWith("pd") || "pd-" in lower
        val isCcBy = lower.startsWith("cc by") || lower.startsWith("cc-by")

        if (isCc0 || isPublicDomain) {
            return Decision(true, false, false)
        }
        if (isCcBy) {
            val shareAlike = lower.contains("sa") || "sharealike" in lower || "share-alike" in lower
            return Decision(true, true, shareAlike)
        }
        return Decision(false, false, false, "Licence outside allow-list")
    }

    fun isAllowed(licenseShortName: String?, usageTerms: String? = null): Boolean =
        evaluate(licenseShortName, usageTerms).allowed
}
