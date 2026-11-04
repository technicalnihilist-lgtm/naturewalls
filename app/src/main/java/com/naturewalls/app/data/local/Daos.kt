package com.naturewalls.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {

    @Upsert
    suspend fun upsertAll(items: List<WallpaperEntity>)

    @Upsert
    suspend fun upsert(item: WallpaperEntity)

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun find(id: Long): WallpaperEntity?

    @Query("SELECT * FROM wallpapers ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun newest(limit: Int): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers ORDER BY cachedAt DESC")
    fun observeAll(): Flow<List<WallpaperEntity>>

    @Query(
        "DELETE FROM wallpapers WHERE cachedAt < :cutoff " +
            "AND id NOT IN (SELECT wallpaperId FROM favorites) " +
            "AND id NOT IN (SELECT wallpaperId FROM downloads)"
    )
    suspend fun pruneUnreferenced(cutoff: Long)

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun count(): Int
}

@Dao
interface LibraryDao {

    // ---- favourites -------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE wallpaperId = :wallpaperId")
    suspend fun removeFavorite(wallpaperId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE wallpaperId = :wallpaperId)")
    suspend fun isFavorite(wallpaperId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE wallpaperId = :wallpaperId)")
    fun observeIsFavorite(wallpaperId: Long): Flow<Boolean>

    @Transaction
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteWithWallpaper>>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoriteCount(): Flow<Int>

    // ---- downloads --------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE wallpaperId = :wallpaperId")
    suspend fun removeDownload(wallpaperId: Long)

    @Query("SELECT * FROM downloads WHERE wallpaperId = :wallpaperId LIMIT 1")
    suspend fun findDownload(wallpaperId: Long): DownloadEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE wallpaperId = :wallpaperId)")
    fun observeIsDownloaded(wallpaperId: Long): Flow<Boolean>

    @Transaction
    @Query("SELECT * FROM downloads ORDER BY savedAt DESC")
    fun observeDownloads(): Flow<List<DownloadWithWallpaper>>

    // ---- recently viewed --------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordView(recent: RecentViewEntity)

    @Transaction
    @Query("SELECT * FROM recent_views ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecentViews(limit: Int): Flow<List<RecentWithWallpaper>>

    @Query(
        "DELETE FROM recent_views WHERE wallpaperId NOT IN " +
            "(SELECT wallpaperId FROM recent_views ORDER BY viewedAt DESC LIMIT :keep)"
    )
    suspend fun trimRecentViews(keep: Int)

    // ---- search history ---------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordSearch(entry: SearchHistoryEntity)

    @Query("SELECT query FROM search_history ORDER BY searchedAt DESC LIMIT :limit")
    fun observeSearchHistory(limit: Int): Flow<List<String>>

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query(
        "DELETE FROM search_history WHERE query NOT IN " +
            "(SELECT query FROM search_history ORDER BY searchedAt DESC LIMIT :keep)"
    )
    suspend fun trimSearchHistory(keep: Int)

    // ---- daily pick -------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyPick(pick: DailyPickEntity)

    @Query("SELECT * FROM daily_picks WHERE day = :day LIMIT 1")
    suspend fun findDailyPick(day: String): DailyPickEntity?
}
