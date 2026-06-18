package dev.yveskalume.newsappp.ui.screens.home

import androidx.compose.runtime.Stable
import dev.yveskalume.newsapp.domain.model.SourceItem

@Stable
sealed interface SourcesUiState {
    data object Loading : SourcesUiState
    data class Success(val sources: List<SourceItem>, val selected: SourceItem?) : SourcesUiState
    data class Error(val message: String) : SourcesUiState
}

sealed interface RefreshUiState {
    data object Idle : RefreshUiState
    data object Refreshing : RefreshUiState
}


