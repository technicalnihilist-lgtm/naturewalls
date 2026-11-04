package com.naturewalls.app.data.model

import java.io.File

/** A wallpaper whose full-resolution file has been downloaded to this device. */
data class DownloadedWallpaper(
    val wallpaper: Wallpaper,
    val filePath: String,
    val savedAt: Long,
    val sizeBytes: Long,
    val inGallery: Boolean
) {
    fun file(): File = File(filePath)

    val exists: Boolean
        get() = file().exists() && file().length() > 0

    val sizeLabel: String
        get() = when {
            sizeBytes <= 0 -> "—"
            sizeBytes < 1024 * 1024 -> String.format("%.0f KB", sizeBytes / 1024.0)
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
}
