package com.saveit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveit.app.data.model.MediaInfo
import com.saveit.app.data.model.Platform
import com.saveit.app.data.repository.CobaltRepository
import com.saveit.app.data.repository.DownloadRepository
import com.saveit.app.data.repository.RedditRepository
import com.saveit.app.util.UrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val url: String = "",
    val platform: Platform = Platform.UNKNOWN,
    val isValidUrl: Boolean = false,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadComplete: Boolean = false,
    val downloadProgress: Float = 0f,
    val mediaInfo: MediaInfo? = null,
    val error: String? = null,
    val selectedQuality: String = "720",
    val audioOnly: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val redditRepository: RedditRepository,
    private val cobaltRepository: CobaltRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onUrlChanged(url: String) {
        val platform = UrlParser.detectPlatform(url)
        val isValid = UrlParser.isValidUrl(url)
        _uiState.value = _uiState.value.copy(
            url = url,
            platform = platform,
            isValidUrl = isValid,
            error = null,
            mediaInfo = null,
            downloadComplete = false,
            isDownloading = false,
            downloadProgress = 0f
        )
    }

    fun onQualityChanged(quality: String) {
        _uiState.value = _uiState.value.copy(selectedQuality = quality)
    }

    fun onAudioOnlyChanged(audioOnly: Boolean) {
        _uiState.value = _uiState.value.copy(audioOnly = audioOnly)
    }

    fun fetchMediaInfo() {
        val currentState = _uiState.value
        if (!currentState.isValidUrl || currentState.isLoading) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null, mediaInfo = null)

            val result = when (currentState.platform) {
                Platform.REDDIT -> redditRepository.getMediaInfo(currentState.url)
                Platform.YOUTUBE, Platform.INSTAGRAM -> cobaltRepository.getMediaInfo(
                    url = currentState.url,
                    platform = currentState.platform,
                    quality = currentState.selectedQuality,
                    audioOnly = currentState.audioOnly
                )
                Platform.UNKNOWN -> Result.failure(Exception("Unsupported platform"))
            }

            result.fold(
                onSuccess = { mediaInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mediaInfo = mediaInfo,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to fetch media info"
                    )
                }
            )
        }
    }

    fun startDownload() {
        val mediaInfo = _uiState.value.mediaInfo ?: return
        if (_uiState.value.isDownloading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                downloadComplete = false,
                error = null,
                downloadProgress = 0f
            )

            val result = downloadRepository.startDownload(mediaInfo)

            result.fold(
                onSuccess = { downloadId ->
                    // Monitor progress
                    viewModelScope.launch {
                        downloadRepository.downloadProgress.collect { progressMap ->
                            val progress = progressMap[downloadId] ?: 0
                            _uiState.value = _uiState.value.copy(
                                downloadProgress = progress / 100f,
                                downloadComplete = progress >= 100,
                                isDownloading = progress < 100
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        error = error.message ?: "Download failed"
                    )
                }
            )
        }
    }

    fun clearState() {
        _uiState.value = HomeUiState()
    }
}
