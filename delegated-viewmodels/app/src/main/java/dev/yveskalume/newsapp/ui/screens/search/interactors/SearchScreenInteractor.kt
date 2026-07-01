package dev.yveskalume.newsapp.ui.screens.search.interactors

import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.search.SearchStateHandler
import dev.yveskalume.newsapp.ui.screens.search.behaviours.SearchBehaviour
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageNumber
import dev.yveskalume.newsapp.util.paging.PageState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchScreenInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: SearchStateHandler,
    private val getArticleBehaviour: GetArticleBehaviour,
    private val searchBehaviour: SearchBehaviour,
) : SearchInteractor {
    private var searchJob: Job? = null

    override fun onQueryChanged(query: String) {
        stateHandler.setQuery(query)

        val normalizedQuery = searchBehaviour.normalizeQuery(query)
        if (!searchBehaviour.canSearch(normalizedQuery)) {
            searchJob?.cancel()
            stateHandler.clearArticles()
            return
        }

        searchJob?.cancel()
        searchJob = scope.launch {
            loadArticles(
                query = normalizedQuery,
                page = PageNumber(1),
            )
        }
    }

    override fun clearSearch() {
        searchJob?.cancel()
        stateHandler.setQuery("")
        stateHandler.clearArticles()
    }

    override fun loadMore() {
        val state = stateHandler.state.value
        val normalizedQuery = searchBehaviour.normalizeQuery(state.query)
        if (!searchBehaviour.canSearch(normalizedQuery)) return
        if (state.articlePageSnapshot.pageState !is PageState.Idle) return
        if (state.articlePageSnapshot.dataState !is DataState.Success) return

        searchJob?.cancel()
        searchJob = scope.launch {
            val nextPage = state.articlePageSnapshot.currentPage?.inc() ?: PageNumber(1)
            loadArticles(
                query = normalizedQuery,
                page = nextPage,
            )
        }
    }

    private suspend fun loadArticles(
        query: String,
        page: PageNumber,
    ) {
        stateHandler.setArticlesLoading(reset = page.value == 1)

        getArticleBehaviour.load(
            snapshot = stateHandler.currentArticlePageSnapshot(),
            query = query,
            page = page,
        ).onSuccess(stateHandler::updateArticlePageSnapshot)
            .onFailure { error ->
                stateHandler.setArticlesError(error.message)
            }
    }
}
