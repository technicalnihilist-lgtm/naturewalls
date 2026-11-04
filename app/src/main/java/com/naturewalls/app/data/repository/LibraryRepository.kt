package com.naturewalls.app.data.repository

import com.naturewalls.app.data.local.DownloadEntity
import com.naturewalls.app.data.local.FavoriteEntity
import com.naturewalls.app.data.local.NatureWallsDatabase
import com.naturewalls.app.data.local.toEntity
import com.naturewalls.app.data.local.toWallpaper
import com.naturewalls.app.data.model.DownloadedWallpaper
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.util.ImageDownloader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * Favourites and downloads. These are the only things that persist locally — the photograph
 * catalogue itself always comes from the live Wikimedia Commons API.
 */
class LibraryRepository(
    private val db: NatureWallsDatabase,
    private val downloader: ImageDownloader
) {

    sealed interface DownloadResult {
        data class Success(val file: File, val inGallery: Boolean) : DownloadResult
        data class Failure(val message: String) : DownloadResult
    }

    fun observeFavorites(): Flow<List<Wallpaper>> =
        db.libraryDao().observeFavorites().map { rows -> rows.map { it.wallpaper.toWallpaper() } }

    fun observeDownloads(): Flow<List<DownloadedWallpaper>> =
        db.libraryDao().observeDownloads().map { rows ->
            rows.map {
                DownloadedWallpaper(
                    wallpaper = it.wallpaper.toWallpaper(),
                    filePath = it.download.filePath,
                    savedAt = it.download.savedAt,
                    sizeBytes = it.download.sizeBytes,
                    inGallery = it.download.inGallery
                )
            }
        }

    fun observeFavoriteCount(): Flow<Int> = db.libraryDao().observeFavoriteCount()

    fun observeIsFavorite(wallpaperId: Long): Flow<Boolean> =
        db.libraryDao().observeIsFavorite(wallpaperId)

    fun observeIsDownloaded(wallpaperId: Long): Flow<Boolean> =
        db.libraryDao().observeIsDownloaded(wallpaperId)

    /** Returns true when the wallpaper is now a favourite. */
    suspend fun toggleFavorite(wallpaper: Wallpaper): Boolean {
        val dao = db.libraryDao()
        return if (dao.isFavorite(wallpaper.id)) {
            dao.removeFavorite(wallpaper.id)
            false
        } else {
            db.wallpaperDao().upsert(wallpaper.toEntity())
            dao.addFavorite(FavoriteEntity(wallpaperId = wallpaper.id, savedAt = System.currentTimeMillis()))
            true
        }
    }

    suspend fun download(wallpaper: Wallpaper): DownloadResult {
        return try {
            db.wallpaperDao().upsert(wallpaper.toEntity())
            val file = downloader.download(wallpaper)
            val inGallery = downloader.publishToGallery(file, galleryName(wallpaper))
            db.libraryDao().addDownload(
                DownloadEntity(
                    wallpaperId = wallpaper.id,
                    filePath = file.absolutePath,
                    savedAt = System.currentTimeMillis(),
                    inGallery = inGallery,
                    sizeBytes = file.length()
                )
            )
            DownloadResult.Success(file, inGallery)
        } catch (t: Throwable) {
            DownloadResult.Failure(t.localizedMessage ?: "Download failed")
        }
    }

    suspend fun removeDownload(wallpaperId: Long): Boolean {
        val existing = db.libraryDao().findDownload(wallpaperId) ?: return false
        runCatching { File(existing.filePath).delete() }
        db.libraryDao().removeDownload(wallpaperId)
        return true
    }

    fun downloadedFile(wallpaper: Wallpaper): File? {
        val file = downloader.permanentFile(wallpaper)
        return if (file.exists() && file.length() > 0) file else null
    }

    private fun galleryName(wallpaper: Wallpaper): String {
        val base = wallpaper.title
            .replace(Regex("[^A-Za-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(48)
            .ifBlank { "nature" }
        return "NatureWalls_${wallpaper.id}_$base.jpg"
    }
}
