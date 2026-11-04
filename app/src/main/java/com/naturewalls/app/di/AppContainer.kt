package com.naturewalls.app.di

import android.content.Context
import com.naturewalls.app.data.local.NatureWallsDatabase
import com.naturewalls.app.data.prefs.UserPreferences
import com.naturewalls.app.data.remote.WikimediaClient
import com.naturewalls.app.data.repository.LibraryRepository
import com.naturewalls.app.data.repository.WallpaperRepository
import com.naturewalls.app.util.ImageDownloader
import com.naturewalls.app.util.WallpaperSetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual dependency container — small, explicit and free of annotation-processor overhead.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    private val api = WikimediaClient.create(appContext.cacheDir)
    private val imageHttp = WikimediaClient.imageClient(appContext.cacheDir)
    private val database = NatureWallsDatabase.get(appContext)

    val preferences = UserPreferences(appContext)
    val imageDownloader = ImageDownloader(appContext, imageHttp)
    val wallpaperSetter = WallpaperSetter(appContext)

    val wallpaperRepository = WallpaperRepository(api, database, preferences)
    val libraryRepository = LibraryRepository(database, imageDownloader)

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun warmUp() {
        applicationScope.launch {
            wallpaperRepository.pruneMetadataCache()
            runCatching { wallpaperRepository.dailyWallpaper() }
        }
    }
}
