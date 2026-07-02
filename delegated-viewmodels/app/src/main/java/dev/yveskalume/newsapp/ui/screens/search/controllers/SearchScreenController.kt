package dev.yveskalume.newsapp.ui.screens.search.controllers

import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.search.SearchStateStore
import dev.yveskalume.newsapp.ui.screens.search.logic.SearchLogic
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageNumber
import dev.yveskalume.newsapp.util.paging.PageState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchScreenController(
    private val scope: CoroutineScope,
    private val stateStore: SearchStateStore,
    private val getArticleLogic: GetArticleLogic,
    private val searchLogic: SearchLogic,
) : SearchController {
    private var searchJob: Job? = null

    override fun onQueryChanged(query: String) {
        stateStore.setQuery(query)

        val normalizedQuery = searchLogic.normalizeQuery(query)
        if (!searchLogic.canSearch(normalizedQuery)) {
            searchJob?.cancel()
            stateStore.clearArticles()
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
        stateStore.setQuery("")
        stateStore.clearArticles()
    }

    override fun loadMore() {
        val state = stateStore.state.value
        val normalizedQuery = searchLogic.normalizeQuery(state.query)
        if (!searchLogic.canSearch(normalizedQuery)) return
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
        stateStore.setArticlesLoading(reset = page.value == 1)

        getArticleLogic.load(
            snapshot = stateStore.currentArticlePageSnapshot(),
            query = query,
            page = page,
        ).onSuccess(stateStore::updateArticlePageSnapshot)
            .onFailure { error ->
                stateStore.setArticlesError(error.message)
            }
    }
}
