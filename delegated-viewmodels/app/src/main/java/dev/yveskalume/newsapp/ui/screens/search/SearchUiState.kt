package dev.yveskalume.newsapp.ui.screens.search

import androidx.compose.runtime.Stable
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageSnapshot

@Stable
data class SearchUiState(
    val query: String,
    val articlePageSnapshot: PageSnapshot<Article>,
) {
    val isLoadingMore: Boolean
        get() = articlePageSnapshot.pageState.isLoading()

    companion object {
        fun initial() = SearchUiState(
            query = "",
            articlePageSnapshot = PageSnapshot(
                dataState = DataState.Success(emptyList()),
            ),
        )
    }
}
