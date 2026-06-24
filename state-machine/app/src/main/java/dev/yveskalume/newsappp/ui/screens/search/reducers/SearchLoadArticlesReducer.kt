package dev.yveskalume.newsappp.ui.screens.search.reducers

import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.core.Reducer
import dev.yveskalume.newsappp.ui.screens.search.SearchEvent
import dev.yveskalume.newsappp.ui.screens.search.SearchResultUiState
import dev.yveskalume.newsappp.ui.screens.search.SearchResultUiState.PagingState
import dev.yveskalume.newsappp.ui.screens.search.SearchUiState

class SearchLoadArticlesReducer(
    private val articleRepository: ArticleRepository
) : Reducer<SearchUiState, SearchEvent.LoadArticles> {

    override suspend fun reduce(
        state: SearchUiState,
        event: SearchEvent.LoadArticles
    ): SearchUiState {
        if (state.query.isBlank()) {
            return state.copy(searchResultUiState = SearchResultUiState.Idle)
        }

        val newState = articleRepository.getTopHeadlines(
            query = state.query,
            page = event.page,
            pageSize = PAGE_SIZE
        ).fold(
            onSuccess = { articles ->
                if (event.page == 1) {
                    mapInitialLoadState(state, articles)
                } else {
                    val current = state.searchResultUiState as? SearchResultUiState.Success ?: return@fold state
                    mapLoadMoreState(current, articles, state, event)
                }
            },
            onFailure = { error ->
                mapErrorState(error, event, state)
            }
        )

        return newState
    }

    private fun mapErrorState(
        error: Throwable,
        event: SearchEvent.LoadArticles,
        state: SearchUiState
    ): SearchUiState {
        val message = error.message ?: "Search failed"
        return if (event.page > 1 && state.searchResultUiState is SearchResultUiState.Success) {
            state.copy(
                searchResultUiState = state.searchResultUiState.copy(
                    pagingState = PagingState.Error(message)
                )
            )
        } else {
            state.copy(
                searchResultUiState = SearchResultUiState.Error(message)
            )
        }
    }

    private fun mapLoadMoreState(
        current: SearchResultUiState.Success,
        articles: List<Article>,
        state: SearchUiState,
        event: SearchEvent.LoadArticles
    ): SearchUiState {
        val merged = current.news + articles
        return state.copy(
            searchResultUiState = current.copy(
                news = merged,
                pagingState = if (articles.isEmpty()) {
                    PagingState.EndReached
                } else {
                    PagingState.Idle(currentPage = event.page)
                }
            )
        )
    }

    private fun mapInitialLoadState(
        state: SearchUiState,
        articles: List<Article>
    ): SearchUiState = state.copy(
        searchResultUiState = if (articles.isEmpty()) {
            SearchResultUiState.Empty
        } else {
            SearchResultUiState.Success(
                news = articles,
                pagingState = PagingState.Idle(currentPage = 1)
            )
        }
    )

    companion object Companion {
        private const val PAGE_SIZE = 20
    }
}

