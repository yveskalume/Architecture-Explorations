package dev.yveskalume.newsapp.ui.screens.home.controllers.impl

import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.home.HomeStateStore
import dev.yveskalume.newsapp.ui.screens.home.controllers.ArticlesController
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageNumber
import dev.yveskalume.newsapp.util.paging.PageState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeArticlesController(
    private val scope: CoroutineScope,
    private val stateStore: HomeStateStore,
    private val getArticleLogic: GetArticleLogic
) : ArticlesController {
    private var retryJob: Job? = null

    override fun retry() {
        retryJob?.cancel()
        retryJob = scope.launch {
            loadArticles(page = PageNumber(1))
        }
    }

    override fun loadMore() {
        val snapshot = stateStore.state.value.articlePageSnapshot
        if (snapshot.pageState !is PageState.Idle) return
        if (snapshot.dataState !is DataState.Success) return

        retryJob?.cancel()
        retryJob = scope.launch {
            val nextPage = snapshot.currentPage?.inc() ?: PageNumber(1)
            loadArticles(page = nextPage)
        }
    }

    private suspend fun loadArticles(
        page: PageNumber,
    ) {
        stateStore.setArticlesLoading(reset = page.value == 1)

        getArticleLogic.load(
            snapshot = stateStore.currentArticlePageSnapshot(),
            sourceId = stateStore.state.value.selectedSource?.id,
            page = page,
        ).onSuccess(stateStore::updateArticlePageSnapshot)
            .onFailure { error ->
                stateStore.setArticlesError(error.message)
            }
    }
}
