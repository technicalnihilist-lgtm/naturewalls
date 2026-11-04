package com.naturewalls.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata cache. It exists so favourites, downloads, recent views, the daily pick and the
 * credits screen can be shown instantly and survive a restart — the images themselves are always
 * fetched live from Wikimedia Commons.
 */
@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: Long,
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
    val categories: String,
    val uploadDate: String?,
    val groupId: String?,
    val subCategoryId: String?,
    val cachedAt: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val wallpaperId: Long,
    val savedAt: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val wallpaperId: Long,
    val filePath: String,
    val savedAt: Long,
    val inGallery: Boolean,
    val sizeBytes: Long
)

@Entity(tableName = "recent_views")
data class RecentViewEntity(
    @PrimaryKey val wallpaperId: Long,
    val viewedAt: Long
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long
)

@Entity(tableName = "daily_picks")
data class DailyPickEntity(
    @PrimaryKey val day: String,
    val wallpaperId: Long,
    val pickedAt: Long
)
