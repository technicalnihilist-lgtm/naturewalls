package com.naturewalls.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.repository.WallpaperRepository
import com.naturewalls.app.ui.browse.FeedUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Live nature-subject search against Wikimedia Commons, with paginated results. */
class SearchViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow(FeedUiState(loading = false))
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private var cursor: String? = null
    private var busy = false

    init {
        viewModelScope.launch {
            repository.observeSearchHistory(8).collect { _history.value = it }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun submit(value: String = _query.value) {
        val trimmed = value.trim()
        if (trimmed.length < 2) {
            _state.value = FeedUiState(loading = false)
            return
        }
        _query.value = trimmed
        cursor = null
        busy = true
        _state.value = FeedUiState(loading = true)
        viewModelScope.launch {
            repository.recordSearch(trimmed)
            val result = runCatching { repository.search(trimmed, limit = 30) }
            result.fold(
                onSuccess = { page ->
                    cursor = page.nextCursor
                    _state.value = FeedUiState(
                        items = page.items,
                        loading = false,
                        endReached = page.nextCursor == null,
                        fetchedAt = page.fetchedAt
                    )
                },
                onFailure = { error ->
                    _state.value = FeedUiState(
                        loading = false,
                        error = error.localizedMessage ?: "Could not reach Wikimedia Commons.",
                        endReached = true
                    )
                }
            )
            busy = false
        }
    }

    fun loadMore() {
        val query = _query.value.trim()
        if (busy || query.length < 2 || cursor == null || _state.value.endReached) return
        busy = true
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            val result = runCatching { repository.search(query, cursor = cursor, limit = 30) }
            result.fold(
                onSuccess = { page ->
                    cursor = page.nextCursor
                    _state.update { state ->
                        val seen = state.items.mapTo(HashSet()) { it.id }
                        val merged = state.items.toMutableList()
                        page.items.forEach { if (seen.add(it.id)) merged.add(it) }
                        state.copy(
                            items = merged,
                            loadingMore = false,
                            error = null,
                            endReached = page.nextCursor == null,
                            fetchedAt = page.fetchedAt
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            loadingMore = false,
                            error = error.localizedMessage ?: "Could not load more results."
                        )
                    }
                }
            )
            busy = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    fun clearResults() {
        cursor = null
        _state.value = FeedUiState(loading = false)
    }

    fun items(): List<Wallpaper> = _state.value.items

    companion object {
        /** Suggested nature subjects — search terms only, results always come from the live API. */
        val SUGGESTED_SUBJECTS = listOf(
            "misty forest",
            "himalayan peaks",
            "waterfall",
            "autumn lake",
            "wildflower meadow",
            "cherry blossom",
            "desert dunes",
            "foggy valley",
            "tropical beach",
            "sunrise"
        )
    }
}
