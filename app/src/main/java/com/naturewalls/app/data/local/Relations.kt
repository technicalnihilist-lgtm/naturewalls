package com.naturewalls.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class FavoriteWithWallpaper(
    @Embedded val favorite: FavoriteEntity,
    @Relation(parentColumn = "wallpaperId", entityColumn = "id")
    val wallpaper: WallpaperEntity
)

data class DownloadWithWallpaper(
    @Embedded val download: DownloadEntity,
    @Relation(parentColumn = "wallpaperId", entityColumn = "id")
    val wallpaper: WallpaperEntity
)

data class RecentWithWallpaper(
    @Embedded val recent: RecentViewEntity,
    @Relation(parentColumn = "wallpaperId", entityColumn = "id")
    val wallpaper: WallpaperEntity
)
