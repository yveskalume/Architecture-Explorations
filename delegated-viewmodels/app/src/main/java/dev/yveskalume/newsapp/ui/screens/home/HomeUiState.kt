package dev.yveskalume.newsapp.ui.screens.home

import androidx.compose.runtime.Stable
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsapp.util.paging.PageSnapshot

@Stable
data class HomeUiState(
    val selectedSource: SourceItem?,
    val sourcesUiState: SourcesUiState,
    val articlePageSnapshot: PageSnapshot<Article>,
    val refreshUiState: RefreshUiState,
) {
    val isLoading: Boolean
        get() = sourcesUiState is SourcesUiState.Loading &&
            articlePageSnapshot.dataState.isLoading()

    val error: String?
        get() {
            val sourcesError = sourcesUiState as? SourcesUiState.Error
            val articlesError = articlePageSnapshot.dataState as? dev.yveskalume.newsapp.util.paging.DataState.Error

            return if (sourcesError != null && articlesError != null) {
                articlesError.message
            } else {
                null
            }
        }

    companion object {
        fun initial() = HomeUiState(
            selectedSource = null,
            sourcesUiState = SourcesUiState.Loading,
            articlePageSnapshot = PageSnapshot(),
            refreshUiState = RefreshUiState.Idle,
        )
    }
}

@Stable
sealed interface SourcesUiState {
    data object Loading : SourcesUiState

    data class Success(
        val sources: List<SourceItem>,
        val selected: SourceItem?,
    ) : SourcesUiState

    data class Error(
        val message: String,
    ) : SourcesUiState
}

@Stable
sealed interface RefreshUiState {
    data object Idle : RefreshUiState
    data object Refreshing : RefreshUiState
}
