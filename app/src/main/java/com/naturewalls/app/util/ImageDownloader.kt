package com.naturewalls.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.remote.WikimediaClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Fetches full-resolution photographs from Wikimedia and keeps the bytes on this device when the
 * user asks for a download. Downloads are real files: they are written to app storage and also
 * published to the system gallery so they survive an uninstall.
 */
class ImageDownloader(
    private val context: Context,
    private val client: OkHttpClient
) {

    private val wallpaperDir: File
        get() = File(context.filesDir, "wallpapers").apply { if (!exists()) mkdirs() }

    private val cacheDir: File
        get() = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }

    fun permanentFile(wallpaper: Wallpaper): File = File(wallpaperDir, "${wallpaper.id}.jpg")

    fun cachedFile(wallpaper: Wallpaper): File = File(cacheDir, "${wallpaper.id}_full.jpg")

    /** Streams the original file to disk. Returns the written file. */
    suspend fun download(wallpaper: Wallpaper, onProgress: (Float) -> Unit = {}): File =
        withContext(Dispatchers.IO) {
            val target = permanentFile(wallpaper)
            if (target.exists() && target.length() > 0) return@withContext target
            fetchToFile(wallpaper.fullUrl, target, onProgress)
        }

    /** Downloads (or reuses) the original, then decodes a screen-sized bitmap for WallpaperManager. */
    suspend fun loadBitmapForScreen(wallpaper: Wallpaper, maxWidth: Int, maxHeight: Int): Bitmap =
        withContext(Dispatchers.IO) {
            val file = permanentFile(wallpaper).takeIf { it.exists() && it.length() > 0 }
                ?: cachedFile(wallpaper).takeIf { it.exists() && it.length() > 0 }
                ?: fetchToFile(wallpaper.fullUrl, cachedFile(wallpaper))
            decodeSampled(file, maxWidth, maxHeight)
        }

    private fun fetchToFile(url: String, target: File, onProgress: (Float) -> Unit = {}): File {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", WikimediaClient.USER_AGENT)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed (HTTP ${response.code})")
            }
            val body = response.body ?: throw IOException("Empty response from Wikimedia")
            val total = body.contentLength()
            val temp = File(target.parentFile, target.name + ".part")

            body.byteStream().use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        written += read
                        if (total > 0) onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }

            if (temp.length() < 1024) {
                temp.delete()
                throw IOException("Downloaded file was empty")
            }
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            return target
        }
    }

    fun decodeSampled(file: File, maxWidth: Int, maxHeight: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Unreadable image file")
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxWidth, maxHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
            ?: throw IOException("Could not decode image")
    }

    private fun sampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var sample = 1
        if (reqWidth <= 0 || reqHeight <= 0) return sample
        while ((width / (sample * 2)) >= reqWidth && (height / (sample * 2)) >= reqHeight) {
            sample *= 2
        }
        return sample
    }

    /** Copies a downloaded file into the device gallery (Pictures/NatureWalls). */
    suspend fun publishToGallery(file: File, displayName: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/NatureWalls"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: return@runCatching false
                    resolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { input -> input.copyTo(out) }
                    } ?: return@runCatching false
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    true
                } else {
                    @Suppress("DEPRECATION")
                    val pictures = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    )
                    val dir = File(pictures, "NatureWalls").apply { mkdirs() }
                    val dest = File(dir, displayName)
                    file.copyTo(dest, overwrite = true)
                    true
                }
            }.getOrDefault(false)
        }

    fun shareUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    fun delete(wallpaper: Wallpaper): Boolean {
        var removed = false
        val permanent = permanentFile(wallpaper)
        if (permanent.exists()) removed = permanent.delete() || removed
        val cached = cachedFile(wallpaper)
        if (cached.exists()) removed = cached.delete() || removed
        return removed
    }
}
