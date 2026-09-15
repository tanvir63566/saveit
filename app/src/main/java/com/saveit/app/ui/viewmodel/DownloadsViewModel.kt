package com.saveit.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saveit.app.data.model.DownloadItem
import com.saveit.app.data.model.Platform
import com.saveit.app.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val downloads: List<DownloadItem> = emptyList(),
    val selectedFilter: Platform? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            downloadRepository.getAllDownloads().collect { downloads ->
                val filtered = _uiState.value.selectedFilter?.let { filter ->
                    downloads.filter { it.platform == filter }
                } ?: downloads

                _uiState.value = _uiState.value.copy(
                    downloads = filtered,
                    isLoading = false
                )
            }
        }
    }

    fun onFilterChanged(platform: Platform?) {
        _uiState.value = _uiState.value.copy(selectedFilter = platform)
        loadDownloads()
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(item)
        }
    }
}
