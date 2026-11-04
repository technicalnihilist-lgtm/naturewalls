package com.naturewalls.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.naturewalls.app.data.remote.WikimediaClient
import com.naturewalls.app.di.AppContainer
import okhttp3.OkHttpClient
import com.naturewalls.app.work.DailyWallpaperWorker

class NatureWallsApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.warmUp()
        DailyWallpaperWorker.schedule(this)
    }

    /**
     * Wikimedia's image hosts answer 403 to a bare `okhttp/x.y.z` User-Agent, so every image
     * request carries the app's descriptive UA through Coil's call factory.
     */
    private val imageNetworkClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", WikimediaClient.USER_AGENT)
                        .build()
                )
            }
            .build()
    }

    /**
     * Coil configuration tuned for large photographs: a 25% memory cache, a 256 MB disk cache and
     * hardware bitmaps so a full-screen 4K nature shot scrolls without jank.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .callFactory(imageNetworkClient)
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil_images"))
                .maxSizeBytes(256L * 1024 * 1024)
                .build()
        }
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .respectCacheHeaders(false)
        .build()
}
