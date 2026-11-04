package com.naturewalls.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.naturewalls.app.data.model.Wallpaper
import java.io.File

/**
 * Sharing always carries the full attribution block, because a CC BY / CC BY-SA photograph must
 * travel with its credit and licence wherever it goes.
 */
object ShareHelper {

    fun attributionText(wallpaper: Wallpaper): String = buildString {
        appendLine(wallpaper.title)
        appendLine(wallpaper.attributionLine)
        wallpaper.licenseUrl?.let { appendLine("Licence: $it") }
        appendLine(wallpaper.sourcePageUrl)
        append("Shared via NatureWalls")
    }

    fun share(context: Context, wallpaper: Wallpaper, imageFile: File?) {
        val text = attributionText(wallpaper)
        val send = Intent(Intent.ACTION_SEND).apply {
            if (imageFile != null && imageFile.exists() && imageFile.length() > 0) {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            putExtra(Intent.EXTRA_SUBJECT, wallpaper.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, "Share wallpaper"))
    }

    fun openUrl(context: Context, url: String?) {
        val safe = url?.takeIf { it.startsWith("http") } ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safe))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
