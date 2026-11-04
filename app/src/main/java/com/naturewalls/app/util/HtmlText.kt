package com.naturewalls.app.util

import android.text.Html
import androidx.core.text.HtmlCompat

/**
 * Commons `extmetadata` values are HTML fragments (links, <i>, <div>…). Attribution must be
 * readable, so tags are stripped and whitespace collapsed before anything reaches the UI.
 */
object HtmlText {

    fun clean(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val noTags = try {
            HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        } catch (t: Throwable) {
            @Suppress("DEPRECATION")
            try {
                Html.fromHtml(raw).toString()
            } catch (ignored: Throwable) {
                raw
            }
        }
        val collapsed = noTags.replace('\u00A0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim(',', ';', '·', '-')
            .trim()
        return collapsed.ifBlank { null }
    }

    /** Cuts a long description to a readable length without breaking mid-word. */
    fun shorten(text: String?, limit: Int = 420): String? {
        val value = text ?: return null
        if (value.length <= limit) return value
        val cut = value.take(limit)
        val lastSpace = cut.lastIndexOf(' ')
        val body = if (lastSpace > limit * 6 / 10) cut.take(lastSpace) else cut
        return body.trimEnd().trimEnd(',', ';', ':') + "…"
    }
}
