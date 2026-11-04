package com.naturewalls.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturewalls.app.data.model.DownloadedWallpaper
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.repository.LibraryRepository
import com.naturewalls.app.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    libraryRepository: LibraryRepository
) : ViewModel() {

    val favorites: StateFlow<List<Wallpaper>> = libraryRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class DownloadsViewModel(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val downloads: StateFlow<List<DownloadedWallpaper>> = libraryRepository.observeDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(wallpaperId: Long) {
        viewModelScope.launch { libraryRepository.removeDownload(wallpaperId) }
    }
}

class CreditsViewModel(
    repository: WallpaperRepository
) : ViewModel() {

    /** Every photograph this device has shown, newest first — the full attribution ledger. */
    val credits: StateFlow<List<Wallpaper>> = repository.observeCredits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
