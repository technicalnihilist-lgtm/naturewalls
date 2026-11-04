package com.naturewalls.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturewalls.app.data.model.CategoryCatalog
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.model.WallpaperGroup
import com.naturewalls.app.data.repository.WallpaperRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionShelf(
    val group: WallpaperGroup,
    val items: List<Wallpaper> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

data class HomeUiState(
    val daily: Wallpaper? = null,
    val dailyLoading: Boolean = true,
    val dailyError: String? = null,
    val featured: List<Wallpaper> = emptyList(),
    val featuredLoading: Boolean = true,
    val featuredError: String? = null,
    val trending: List<Wallpaper> = emptyList(),
    val trendingLoading: Boolean = true,
    val trendingError: String? = null,
    val recentlyAdded: List<Wallpaper> = emptyList(),
    val recentLoading: Boolean = true,
    val recentError: String? = null,
    val collections: List<CollectionShelf> = emptyList(),
    val recentViews: List<Wallpaper> = emptyList(),
    val fetchedAt: Long = 0L,
    val refreshing: Boolean = false
) {
    val firstLoad: Boolean
        get() = featured.isEmpty() && daily == null && featuredLoading
}

class HomeViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeUiState(
            collections = CategoryCatalog.groups.map { CollectionShelf(it) }
        )
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var lastLoadedAt = 0L

    init {
        observeRecentViews()
        observeFetchTime()
        load()
    }

    private fun observeRecentViews() {
        viewModelScope.launch {
            repository.observeRecentViews(12).collect { views ->
                _state.update { it.copy(recentViews = views) }
            }
        }
    }

    private fun observeFetchTime() {
        viewModelScope.launch {
            repository.observeLastLiveFetch().collect { at ->
                _state.update { it.copy(fetchedAt = at) }
            }
        }
    }

    /** Every shelf is a fresh API call. [force] is only used by the refresh button. */
    fun load(force: Boolean = false) {
        if (_state.value.refreshing) return
        lastLoadedAt = System.currentTimeMillis()
        _state.update { current ->
            current.copy(
                refreshing = true,
                dailyLoading = current.daily == null,
                featuredLoading = current.featured.isEmpty(),
                trendingLoading = current.trending.isEmpty(),
                recentLoading = current.recentlyAdded.isEmpty(),
                collections = current.collections.map {
                    it.copy(loading = it.items.isEmpty(), error = null)
                }
            )
        }

        viewModelScope.launch {
            val daily = async { runCatching { repository.dailyWallpaper() } }
            val featured = async { runCatching { repository.featured(24) } }
            val trending = async { runCatching { repository.trending(limit = 10) } }
            val recent = async { runCatching { repository.recentlyAdded(20) } }

            runCatching { daily.await() }.getOrNull()?.let { result ->
                _state.update {
                    it.copy(
                        daily = result.getOrNull() ?: it.daily,
                        dailyLoading = false,
                        dailyError = result.exceptionOrNull()?.let { e -> friendly(e) }
                    )
                }
            }

            runCatching { featured.await() }.getOrNull()?.let { result ->
                _state.update {
                    it.copy(
                        featured = result.getOrNull() ?: emptyList(),
                        featuredLoading = false,
                        featuredError = result.exceptionOrNull()?.let { e -> friendly(e) }
                    )
                }
            }

            runCatching { trending.await() }.getOrNull()?.let { result ->
                _state.update {
                    it.copy(
                        trending = result.getOrNull() ?: emptyList(),
                        trendingLoading = false,
                        trendingError = result.exceptionOrNull()?.let { e -> friendly(e) }
                    )
                }
            }

            runCatching { recent.await() }.getOrNull()?.let { result ->
                _state.update {
                    it.copy(
                        recentlyAdded = result.getOrNull() ?: emptyList(),
                        recentLoading = false,
                        recentError = result.exceptionOrNull()?.let { e -> friendly(e) }
                    )
                }
            }

            // Collection shelves, one live search each.
            val shelves = _state.value.collections
            shelves.forEachIndexed { index, shelf ->
                launch {
                    val result = runCatching {
                        repository.search(shelf.group.coverQuery, limit = 12, groupId = shelf.group.id)
                    }
                    _state.update { current ->
                        val updated = current.collections.toMutableList()
                        if (index < updated.size) {
                            updated[index] = shelf.copy(
                                items = result.getOrNull()?.items ?: emptyList(),
                                loading = false,
                                error = result.exceptionOrNull()?.let { friendly(it) }
                            )
                        }
                        current.copy(collections = updated)
                    }
                }
            }

            _state.update { it.copy(refreshing = false) }
        }
    }

    /** Called when the home screen resumes; keeps the feed live without hammering the API. */
    fun refreshIfStale() {
        val age = System.currentTimeMillis() - lastLoadedAt
        if (age > LIVE_WINDOW_MS) load()
    }

    private fun friendly(error: Throwable): String =
        error.localizedMessage?.takeIf { it.isNotBlank() }
            ?: "Could not reach Wikimedia Commons."

    companion object {
        private const val LIVE_WINDOW_MS = 2 * 60 * 1000L
    }
}
