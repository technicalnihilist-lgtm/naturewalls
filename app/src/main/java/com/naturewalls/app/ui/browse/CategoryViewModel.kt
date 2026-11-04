package com.naturewalls.app.ui.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturewalls.app.data.model.CategoryCatalog
import com.naturewalls.app.data.model.SubCategory
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.model.WallpaperGroup
import com.naturewalls.app.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val items: List<Wallpaper> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val fetchedAt: Long = 0L
)

/**
 * Paginated, live category browsing. Sub-category switches restart the feed with a new Commons
 * search; scrolling to the end follows the API's `continue` cursor.
 */
class CategoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WallpaperRepository
) : ViewModel() {

    val group: WallpaperGroup? = CategoryCatalog.group(savedStateHandle.get<String>("groupId"))

    private val _selectedSubId = MutableStateFlow(
        savedStateHandle.get<String>("subId") ?: group?.subCategories?.firstOrNull()?.id
    )
    val selectedSubId: StateFlow<String?> = _selectedSubId.asStateFlow()

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private var cursor: String? = null
    private var loading = false

    init {
        reload()
    }

    fun selectSubCategory(subCategory: SubCategory) {
        if (_selectedSubId.value == subCategory.id) return
        _selectedSubId.value = subCategory.id
        reload()
    }

    fun reload() {
        cursor = null
        _state.value = FeedUiState(loading = true)
        load(reset = true)
    }

    fun loadMore() {
        if (loading || _state.value.endReached || cursor == null) return
        load(reset = false)
    }

    private fun load(reset: Boolean) {
        val currentGroup = group ?: return
        val sub = currentGroup.subCategory(_selectedSubId.value)
        val query = sub?.searchQuery ?: currentGroup.coverQuery
        loading = true
        if (!reset) _state.update { it.copy(loadingMore = true) }

        viewModelScope.launch {
            val result = runCatching {
                repository.search(
                    query = query,
                    cursor = if (reset) null else cursor,
                    limit = 30,
                    groupId = currentGroup.id,
                    subCategoryId = sub?.id
                )
            }
            result.fold(
                onSuccess = { page ->
                    cursor = page.nextCursor
                    _state.update { state ->
                        val merged = if (reset) page.items else mergeDistinct(state.items, page.items)
                        state.copy(
                            items = merged,
                            loading = false,
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
                            loading = false,
                            loadingMore = false,
                            error = error.localizedMessage ?: "Could not reach Wikimedia Commons.",
                            endReached = reset && it.items.isEmpty()
                        )
                    }
                }
            )
            loading = false
        }
    }

    private fun mergeDistinct(existing: List<Wallpaper>, incoming: List<Wallpaper>): List<Wallpaper> {
        val seen = existing.mapTo(HashSet()) { it.id }
        val merged = existing.toMutableList()
        incoming.forEach { if (seen.add(it.id)) merged.add(it) }
        return merged
    }
}
