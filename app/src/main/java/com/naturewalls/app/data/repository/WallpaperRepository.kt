package com.naturewalls.app.data.repository

import com.naturewalls.app.data.local.DailyPickEntity
import com.naturewalls.app.data.local.NatureWallsDatabase
import com.naturewalls.app.data.local.RecentViewEntity
import com.naturewalls.app.data.local.SearchHistoryEntity
import com.naturewalls.app.data.local.toEntity
import com.naturewalls.app.data.local.toWallpaper
import com.naturewalls.app.data.model.CategoryCatalog
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.prefs.UserPreferences
import com.naturewalls.app.data.remote.CommonsResponse
import com.naturewalls.app.data.remote.WikimediaApi
import com.naturewalls.app.data.remote.WallpaperMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

/**
 * The live content source of NatureWalls.
 *
 * Every call here goes to the Wikimedia Commons API at the moment the user looks at the screen.
 * Nothing is bundled, nothing is seeded from a fixture file. The Room metadata cache only keeps
 * attribution data for things the user has already seen (favourites, downloads, credits).
 */
class WallpaperRepository(
    private val api: WikimediaApi,
    private val db: NatureWallsDatabase,
    private val prefs: UserPreferences
) {

    data class Page(
        val items: List<Wallpaper>,
        val nextCursor: String?,
        val fetchedAt: Long
    )

    class CommonsException(message: String) : IOException(message)

    /** Dedupe state per feed key, so a repeated image never appears twice in one list. */
    private val seenIds = HashMap<String, MutableSet<Long>>()

    // ------------------------------------------------------------------ search

    suspend fun search(
        query: String,
        cursor: String? = null,
        limit: Int = 30,
        groupId: String? = null,
        subCategoryId: String? = null
    ): Page {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return Page(emptyList(), null, System.currentTimeMillis())
        val base = baseParams().apply {
            put("generator", "search")
            put("gsrsearch", "$trimmed filetype:bitmap")
            put("gsrnamespace", "6")
            put("gsrlimit", limit.coerceIn(1, 50).toString())
        }
        return fetch(base, cursor, "search:${trimmed.lowercase(Locale.ROOT)}", groupId, subCategoryId)
    }

    suspend fun category(
        category: String,
        cursor: String? = null,
        limit: Int = 30,
        newestFirst: Boolean = false,
        groupId: String? = null,
        subCategoryId: String? = null
    ): Page {
        val base = baseParams().apply {
            put("generator", "categorymembers")
            put("gcmtitle", "Category:$category")
            put("gcmtype", "file")
            put("gcmlimit", limit.coerceIn(1, 50).toString())
            if (newestFirst) {
                put("gcmsort", "timestamp")
                put("gcmdir", "desc")
            }
        }
        val key = "cat:$category:${if (newestFirst) "new" else "std"}"
        return fetch(base, cursor, key, groupId, subCategoryId)
    }

    // ------------------------------------------------------------- curated feeds

    /** Commons "Quality images of nature" — the editorial hero shelf. */
    suspend fun featured(limit: Int = 24): List<Wallpaper> =
        category(CategoryCatalog.CURATED_NATURE_CATEGORY, limit = limit).items

    /** Newly promoted nature photographs — genuinely recent uploads, not a static list. */
    suspend fun recentlyAdded(limit: Int = 24): List<Wallpaper> =
        category(CategoryCatalog.CURATED_NATURE_CATEGORY, limit = limit, newestFirst = true).items

    /** Rotating editorial queries, refreshed as the day changes. */
    suspend fun trending(dayOfYear: Int = todayOfYear(), limit: Int = 12): List<Wallpaper> {
        val queries = CategoryCatalog.trendingQueries
        val start = (dayOfYear % queries.size + queries.size) % queries.size
        val picked = listOf(
            queries[start],
            queries[(start + 3) % queries.size],
            queries[(start + 6) % queries.size]
        )
        val out = LinkedHashMap<Long, Wallpaper>()
        for (query in picked) {
            runCatching { search(query, limit = limit).items }
                .getOrDefault(emptyList())
                .forEach { out.putIfAbsent(it.id, it) }
        }
        return out.values.toList()
    }

    /** One deterministic, freshly fetched wallpaper per calendar day. */
    suspend fun dailyWallpaper(day: String = today()): Wallpaper? {
        val existing = db.libraryDao().findDailyPick(day)
        if (existing != null) {
            db.wallpaperDao().find(existing.wallpaperId)?.let { return it.toWallpaper() }
        }
        val candidates = runCatching {
            category(CategoryCatalog.CURATED_NATURE_CATEGORY, limit = 40, newestFirst = true).items
        }.getOrDefault(emptyList()).ifEmpty {
            runCatching { featured(limit = 40) }.getOrDefault(emptyList())
        }
        if (candidates.isEmpty()) return null
        val index = ((day.hashCode().toLong() and 0x7fffffffL) % candidates.size).toInt()
        val pick = candidates[index]
        db.libraryDao().saveDailyPick(
            DailyPickEntity(day = day, wallpaperId = pick.id, pickedAt = System.currentTimeMillis())
        )
        return pick
    }

    suspend fun wallpaperById(id: Long): Wallpaper? {
        db.wallpaperDao().find(id)?.let { return it.toWallpaper() }
        val params = baseParams().apply { put("pageids", id.toString()) }
        val response = runCatching { api.query(params) }.getOrNull() ?: return null
        val page = response.query?.pages?.firstOrNull() ?: return null
        val wallpaper = WallpaperMapper.map(page) ?: return null
        db.wallpaperDao().upsert(wallpaper.toEntity())
        return wallpaper
    }

    // ------------------------------------------------------------------ library

    suspend fun recordView(wallpaper: Wallpaper) {
        db.wallpaperDao().upsert(wallpaper.toEntity())
        db.libraryDao().recordView(
            RecentViewEntity(wallpaperId = wallpaper.id, viewedAt = System.currentTimeMillis())
        )
        db.libraryDao().trimRecentViews(keep = 40)
    }

    fun observeRecentViews(limit: Int = 20): Flow<List<Wallpaper>> =
        db.libraryDao().observeRecentViews(limit).map { rows ->
            rows.map { it.wallpaper.toWallpaper() }
        }

    fun observeCredits(): Flow<List<Wallpaper>> =
        db.wallpaperDao().observeAll().map { rows -> rows.map { it.toWallpaper() } }

    suspend fun recordSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        db.libraryDao().recordSearch(
            SearchHistoryEntity(query = trimmed, searchedAt = System.currentTimeMillis())
        )
        db.libraryDao().trimSearchHistory(keep = 12)
    }

    fun observeSearchHistory(limit: Int = 8): Flow<List<String>> =
        db.libraryDao().observeSearchHistory(limit)

    suspend fun clearSearchHistory() = db.libraryDao().clearSearchHistory()

    fun observeLastLiveFetch(): Flow<Long> = prefs.lastLiveFetch

    /** Drops old metadata rows, keeping favourites and downloads forever. */
    suspend fun pruneMetadataCache() {
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        runCatching { db.wallpaperDao().pruneUnreferenced(cutoff) }
    }

    // ------------------------------------------------------------------ plumbing

    private fun baseParams(): MutableMap<String, String> = mutableMapOf(
        "action" to "query",
        "format" to "json",
        "formatversion" to "2",
        "prop" to "imageinfo",
        "iiprop" to "url|size|extmetadata|timestamp|mime",
        "iiurlwidth" to "500",
        "iilimit" to "1",
        "maxlag" to "5"
    )

    /**
     * Runs the request, and keeps asking for the next page while the licence/quality filter has
     * dropped so many results that the caller would get a half-empty screen.
     */
    private suspend fun fetch(
        base: Map<String, String>,
        cursor: String?,
        dedupeKey: String,
        groupId: String?,
        subCategoryId: String?
    ): Page {
        if (cursor == null) seenIds[dedupeKey] = mutableSetOf()
        val seen = seenIds.getOrPut(dedupeKey) { mutableSetOf() }

        val items = ArrayList<Wallpaper>()
        var next: String? = cursor
        var fetchedAt = System.currentTimeMillis()
        var attempts = 0

        while (attempts < MAX_FILL_ATTEMPTS) {
            val response: CommonsResponse = requestOnce(base, next)

            for (page in response.query?.pages.orEmpty()) {
                val wallpaper = WallpaperMapper.map(page, groupId, subCategoryId) ?: continue
                if (!seen.add(wallpaper.id)) continue
                items.add(wallpaper)
            }
            next = response.continueParams?.let { encodeCursor(it) }
            fetchedAt = System.currentTimeMillis()
            attempts++

            if (items.size >= MIN_ITEMS_PER_PAGE || next == null) break
        }

        if (items.isNotEmpty()) {
            runCatching { db.wallpaperDao().upsertAll(items.map { it.toEntity() }) }
        }
        runCatching { prefs.setLastLiveFetch(fetchedAt) }
        return Page(items = items, nextCursor = next, fetchedAt = fetchedAt)
    }

    private suspend fun requestOnce(
        base: Map<String, String>,
        cursor: String?
    ): CommonsResponse {
        val params = HashMap(base)
        if (cursor != null) params.putAll(decodeCursor(cursor))
        val response = try {
            api.query(params)
        } catch (io: IOException) {
            throw CommonsException("Network problem reaching Wikimedia Commons. Check your connection.")
        } catch (t: Throwable) {
            throw CommonsException("Wikimedia Commons returned an unexpected response.")
        }
        response.error?.let {
            throw CommonsException(it.info ?: "Wikimedia API error (${it.code ?: "unknown"})")
        }
        return response
    }

    private fun encodeCursor(params: Map<String, String>): String =
        params.entries.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }

    private fun decodeCursor(cursor: String): Map<String, String> =
        cursor.split("&").mapNotNull { pair ->
            val index = pair.indexOf('=')
            if (index <= 0) null
            else pair.substring(0, index) to URLDecoder.decode(pair.substring(index + 1), "UTF-8")
        }.toMap()

    companion object {
        private const val MAX_FILL_ATTEMPTS = 4
        private const val MIN_ITEMS_PER_PAGE = 6

        fun today(): String = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(java.util.Date())

        fun todayOfYear(): Int {
            val calendar = java.util.Calendar.getInstance()
            return calendar.get(java.util.Calendar.DAY_OF_YEAR)
        }
    }
}
