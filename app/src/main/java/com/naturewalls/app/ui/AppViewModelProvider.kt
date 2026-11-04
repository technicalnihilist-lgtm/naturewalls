package com.naturewalls.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.naturewalls.app.NatureWallsApp
import com.naturewalls.app.ui.browse.CategoryViewModel
import com.naturewalls.app.ui.detail.DetailViewModel
import com.naturewalls.app.ui.favorites.CreditsViewModel
import com.naturewalls.app.ui.favorites.DownloadsViewModel
import com.naturewalls.app.ui.favorites.FavoritesViewModel
import com.naturewalls.app.ui.home.HomeViewModel
import com.naturewalls.app.ui.search.SearchViewModel

/** One factory for every screen ViewModel, wired to the manual [AppContainer]. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { HomeViewModel(app().container.wallpaperRepository) }

        initializer {
            CategoryViewModel(
                savedStateHandle = createSavedStateHandle(),
                repository = app().container.wallpaperRepository
            )
        }

        initializer { SearchViewModel(app().container.wallpaperRepository) }

        initializer {
            val container = app().container
            DetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                wallpaperRepository = container.wallpaperRepository,
                libraryRepository = container.libraryRepository,
                downloader = container.imageDownloader,
                wallpaperSetter = container.wallpaperSetter
            )
        }

        initializer { FavoritesViewModel(app().container.libraryRepository) }
        initializer { DownloadsViewModel(app().container.libraryRepository) }
        initializer { CreditsViewModel(app().container.wallpaperRepository) }
    }
}

private fun CreationExtras.app(): NatureWallsApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NatureWallsApp
