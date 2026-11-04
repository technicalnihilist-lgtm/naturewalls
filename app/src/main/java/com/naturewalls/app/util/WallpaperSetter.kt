package com.naturewalls.app.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class WallpaperTarget(val label: String) {
    HOME("Home screen"),
    LOCK("Lock screen"),
    BOTH("Home and lock screen")
}

sealed interface WallpaperResult {
    data object Success : WallpaperResult
    data class Failure(val message: String) : WallpaperResult
}

/** Thin, well-behaved wrapper around [WallpaperManager]. */
class WallpaperSetter(private val context: Context) {

    private val manager: WallpaperManager?
        get() = WallpaperManager.getInstance(context)

    fun isSupported(): Boolean {
        val wm = manager ?: return false
        return wm.isWallpaperSupported && runCatching { wm.isSetWallpaperAllowed }.getOrDefault(true)
    }

    suspend fun apply(bitmap: Bitmap, target: WallpaperTarget): WallpaperResult =
        withContext(Dispatchers.IO) {
            val wm = manager
                ?: return@withContext WallpaperResult.Failure("Wallpaper service unavailable")
            if (!wm.isWallpaperSupported) {
                return@withContext WallpaperResult.Failure(
                    "This device does not support wallpaper changes"
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !wm.isSetWallpaperAllowed) {
                return@withContext WallpaperResult.Failure(
                    "Wallpaper changes are blocked by device policy"
                )
            }
            val flags = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
            val result = runCatching {
                wm.setBitmap(bitmap, null, true, flags)
            }
            if (result.isSuccess) {
                WallpaperResult.Success
            } else {
                val reason = result.exceptionOrNull()?.localizedMessage
                WallpaperResult.Failure(reason ?: "Could not set the wallpaper on this device")
            }
        }
}
