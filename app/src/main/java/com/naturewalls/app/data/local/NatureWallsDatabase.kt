package com.naturewalls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WallpaperEntity::class,
        FavoriteEntity::class,
        DownloadEntity::class,
        RecentViewEntity::class,
        SearchHistoryEntity::class,
        DailyPickEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NatureWallsDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var instance: NatureWallsDatabase? = null

        fun get(context: Context): NatureWallsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NatureWallsDatabase::class.java,
                    "naturewalls.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
