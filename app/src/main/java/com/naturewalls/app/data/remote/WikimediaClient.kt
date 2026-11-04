package com.naturewalls.app.data.remote

import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Networking for Wikimedia Commons.
 *
 * Wikimedia asks every client to identify itself with a descriptive User-Agent, so it is set on
 * every request. A retry interceptor handles the transient 429 / 5xx responses the API can return
 * under load, and a 48 MB HTTP cache lets repeated page loads resolve without hitting the network.
 */
object WikimediaClient {

    const val BASE_URL = "https://commons.wikimedia.org/"

    /** Descriptive User-Agent required by the Wikimedia API etiquette policy. */
    const val USER_AGENT =
        "NatureWalls/1.0.0 (Android wallpaper app; com.naturewalls.app) " +
            "OkHttp/4.12.0 contact:nihaonihao54059@gmail.com"

    private val RETRYABLE_CODES = setOf(408, 425, 429, 500, 502, 503, 504)

    class RetryInterceptor(private val maxRetries: Int = 2) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var attempt = 0
            while (true) {
                try {
                    val response = chain.proceed(request)
                    if (response.code in RETRYABLE_CODES && attempt < maxRetries) {
                        response.close()
                        attempt++
                        backoff(attempt)
                        continue
                    }
                    return response
                } catch (io: IOException) {
                    if (attempt >= maxRetries) throw io
                    attempt++
                    backoff(attempt)
                }
            }
        }

        private fun backoff(attempt: Int) {
            try {
                Thread.sleep(350L * attempt * attempt)
            } catch (ignored: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    fun create(cacheDir: File): WikimediaApi {
        val httpCache = Cache(File(cacheDir, "commons_http"), 48L * 1024 * 1024)

        val client = OkHttpClient.Builder()
            .cache(httpCache)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(headerInterceptor())
            .addInterceptor(RetryInterceptor())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikimediaApi::class.java)
    }

    private fun headerInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()
        chain.proceed(request)
    }

    /** Shared client for full-size image downloads (separate, larger cache). */
    fun imageClient(cacheDir: File): OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(File(cacheDir, "commons_images"), 64L * 1024 * 1024))
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(headerInterceptor())
        .addInterceptor(RetryInterceptor())
        .build()
}
