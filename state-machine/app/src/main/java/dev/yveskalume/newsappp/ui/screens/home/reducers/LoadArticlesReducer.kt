package dev.yveskalume.newsappp.ui.screens.home.reducers

import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.core.Reducer
import dev.yveskalume.newsappp.ui.screens.home.HomeEvent
import dev.yveskalume.newsappp.ui.screens.home.HomeUiState
import dev.yveskalume.newsappp.ui.screens.home.ArticleUiState
import dev.yveskalume.newsappp.ui.screens.home.ArticleUiState.PagingState

class LoadArticlesReducer(
    private val articleRepository: ArticleRepository
) : Reducer<HomeUiState, HomeEvent.LoadArticles> {
    override suspend fun reduce(
        state: HomeUiState,
        event: HomeEvent.LoadArticles
    ): HomeUiState {
        val newState = articleRepository.getTopHeadlines(
            sources = state.selectedSource?.id,
            page = event.page,
            pageSize = PAGE_SIZE
        ).fold(
            onSuccess = { newArticles ->
                ArticleUiState.Success(
                    articles = mergeArticles(state, event, newArticles),
                    pagingState = createPagingState(newArticles, event)
                )
            },
            onFailure = { error ->
                formatFailedState(state, event, error)
            }
        )
        return state.copy(articleUiState = newState)
    }

    private fun mergeArticles(
        state: HomeUiState,
        event: HomeEvent.LoadArticles,
        articles: List<Article>
    ): List<Article> {
        val merged = if (state.articleUiState is ArticleUiState.Success && event.page > 1) {
            state.articleUiState.articles + articles
        } else {
            articles
        }
        return merged
    }

    private fun createPagingState(
        articles: List<Article>,
        event: HomeEvent.LoadArticles
    ): PagingState = if (articles.isEmpty()) {
        PagingState.EndReached
    } else {
        PagingState.Idle(currentPage = event.page)
    }

    private fun formatFailedState(
        state: HomeUiState,
        event: HomeEvent.LoadArticles,
        error: Throwable
    ): ArticleUiState = if (state.articleUiState is ArticleUiState.Success && event.page > 1) {
        state.articleUiState.copy(
            pagingState = PagingState.Error(
                error.message ?: "Failed to load news"
            )
        )
    } else {
        ArticleUiState.Error(
            message = error.message ?: "Failed to load news",
        )
    }


    companion object {
        private const val PAGE_SIZE = 20
    }
}