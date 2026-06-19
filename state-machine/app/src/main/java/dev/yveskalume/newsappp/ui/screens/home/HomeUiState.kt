package dev.yveskalume.newsappp.ui.screens.home

import androidx.compose.runtime.Stable
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsappp.core.State

@Stable
data class HomeUiState(
    val selectedSource: SourceItem?,
    val sourcesUiState: SourcesUiState,
    val articleUiState: ArticleUiState,
    val refreshUiState: RefreshUiState
) : State {
    companion object {
        fun initial() = HomeUiState(
            selectedSource = null,
            sourcesUiState = SourcesUiState.Loading,
            articleUiState = ArticleUiState.Loading,
            refreshUiState = RefreshUiState.Idle
        )
    }
}


@Stable
sealed interface SourcesUiState {
    data object Loading : SourcesUiState
    data class Success(val sources: List<SourceItem>,val selected: SourceItem? = null) : SourcesUiState
    data class Error(val message: String) : SourcesUiState
}

@Stable
sealed interface ArticleUiState {
    data object Loading : ArticleUiState

    data class Success(
        val isRefreshing: Boolean = false,
        val articles: List<Article>,
        val pagingState: PagingState = PagingState.Idle(currentPage = 1),
    ) : ArticleUiState

    data class Error(
        val message: String,
    ) : ArticleUiState


    @Stable
    sealed interface PagingState {
        data class Idle(val currentPage: Int) : PagingState
        data object Loading : PagingState
        data class Error(val message: String) : PagingState
        data object EndReached : PagingState
    }

    val isLoadingMore: Boolean
        get() {
            return when (this) {
                is Success -> this.pagingState is PagingState.Loading
                else -> false
            }
        }

    val canLoadMore: Boolean
        get() {
            return when (this) {
                is Success -> this.pagingState !is PagingState.EndReached && this.pagingState !is PagingState.Loading
                else -> false
            }
        }
}

sealed interface RefreshUiState {
    data object Idle : RefreshUiState
    data object Refreshing : RefreshUiState
}


