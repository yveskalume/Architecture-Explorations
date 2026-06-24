package dev.yveskalume.newsappp.ui.screens.search

import androidx.compose.runtime.Stable
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.core.State

@Stable
data class SearchUiState(
    val query: String,
    val searchResultUiState: SearchResultUiState
) : State {
    companion object {
        fun initial() = SearchUiState(
            query = "",
            searchResultUiState = SearchResultUiState.Idle
        )
    }


    val isLoadingMore: Boolean
        get() = (searchResultUiState as? SearchResultUiState.Success)?.pagingState is SearchResultUiState.PagingState.Loading

    val canLoadMore: Boolean
        get() {
            val paging = (searchResultUiState as? SearchResultUiState.Success)?.pagingState ?: return false
            return paging !is SearchResultUiState.PagingState.EndReached &&
                    paging !is SearchResultUiState.PagingState.Loading
        }
}

@Stable
sealed interface SearchResultUiState {
    data object Idle : SearchResultUiState

    data object Loading : SearchResultUiState

    data class Success(
        val news: List<Article>,
        val pagingState: PagingState = PagingState.Idle(currentPage = 1)
    ) : SearchResultUiState

    /** Search completed successfully, but no articles matched. */
    data object Empty : SearchResultUiState

    data class Error(
        val message: String
    ) : SearchResultUiState

    sealed interface PagingState {
        data class Idle(val currentPage: Int) : PagingState
        data object Loading : PagingState
        data class Error(val message: String) : PagingState
        data object EndReached : PagingState
    }
}
