package com.naturewalls.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturewalls.app.data.model.Wallpaper
import com.naturewalls.app.data.repository.LibraryRepository
import com.naturewalls.app.data.repository.WallpaperRepository
import com.naturewalls.app.util.ImageDownloader
import com.naturewalls.app.util.WallpaperResult
import com.naturewalls.app.util.WallpaperSetter
import com.naturewalls.app.util.WallpaperTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class DetailUiState(
    val wallpaper: Wallpaper? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val busyAction: String? = null,
    val message: String? = null
)

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val wallpaperRepository: WallpaperRepository,
    private val libraryRepository: LibraryRepository,
    private val downloader: ImageDownloader,
    private val wallpaperSetter: WallpaperSetter
) : ViewModel() {

    private val wallpaperId: Long =
        savedStateHandle.get<String>("wallpaperId")?.toLongOrNull() ?: 0L

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        loadWallpaper()
        observeLibraryFlags()
    }

    private fun loadWallpaper() {
        viewModelScope.launch {
            val result = runCatching { wallpaperRepository.wallpaperById(wallpaperId) }
            result.fold(
                onSuccess = { wallpaper ->
                    if (wallpaper == null) {
                        _state.update {
                            it.copy(
                                loading = false,
                                error = "This photograph is no longer available on Wikimedia Commons."
                            )
                        }
                    } else {
                        _state.update { it.copy(wallpaper = wallpaper, loading = false) }
                        wallpaperRepository.recordView(wallpaper)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.localizedMessage ?: "Could not load this wallpaper."
                        )
                    }
                }
            )
        }
    }

    private fun observeLibraryFlags() {
        viewModelScope.launch {
            libraryRepository.observeIsFavorite(wallpaperId).collect { fav ->
                _state.update { it.copy(isFavorite = fav) }
            }
        }
        viewModelScope.launch {
            libraryRepository.observeIsDownloaded(wallpaperId).collect { downloaded ->
                _state.update { it.copy(isDownloaded = downloaded) }
            }
        }
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            val nowFavorite = libraryRepository.toggleFavorite(wallpaper)
            _state.update {
                it.copy(
                    message = if (nowFavorite) {
                        "Saved to favourites — credit travels with it"
                    } else {
                        "Removed from favourites"
                    }
                )
            }
        }
    }

    fun download() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.busyAction != null) return
        _state.update { it.copy(busyAction = "Downloading original") }
        viewModelScope.launch {
            when (val result = libraryRepository.download(wallpaper)) {
                is LibraryRepository.DownloadResult.Success -> _state.update {
                    it.copy(
                        busyAction = null,
                        isDownloaded = true,
                        message = if (result.inGallery) {
                            "Saved to Pictures/NatureWalls"
                        } else {
                            "Saved inside NatureWalls"
                        }
                    )
                }

                is LibraryRepository.DownloadResult.Failure -> _state.update {
                    it.copy(busyAction = null, message = result.message)
                }
            }
        }
    }

    fun setWallpaper(target: WallpaperTarget, screenWidth: Int, screenHeight: Int) {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.busyAction != null) return
        _state.update { it.copy(busyAction = "Preparing ${target.label.lowercase()}") }
        viewModelScope.launch {
            val result = runCatching {
                downloader.loadBitmapForScreen(wallpaper, screenWidth, screenHeight)
            }
            result.fold(
                onSuccess = { bitmap ->
                    when (val applied = wallpaperSetter.apply(bitmap, target)) {
                        is WallpaperResult.Success -> _state.update {
                            it.copy(busyAction = null, message = "${target.label} wallpaper set")
                        }

                        is WallpaperResult.Failure -> _state.update {
                            it.copy(busyAction = null, message = applied.message)
                        }
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            busyAction = null,
                            message = error.localizedMessage ?: "Could not fetch the full-size image."
                        )
                    }
                }
            )
        }
    }

    suspend fun localImageFile(): File? {
        val wallpaper = _state.value.wallpaper ?: return null
        return libraryRepository.downloadedFile(wallpaper)
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
