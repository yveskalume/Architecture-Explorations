package dev.yveskalume.newsapp.ui.screens.home.interactors.impl

import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.home.HomeStateHandler
import dev.yveskalume.newsapp.ui.screens.home.interactors.ArticlesInteractor
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageNumber
import dev.yveskalume.newsapp.util.paging.PageState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeArticlesInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: HomeStateHandler,
    private val getArticleBehaviour: GetArticleBehaviour
) : ArticlesInteractor {
    private var retryJob: Job? = null

    override fun retry() {
        retryJob?.cancel()
        retryJob = scope.launch {
            loadArticles(page = PageNumber(1))
        }
    }

    override fun loadMore() {
        val snapshot = stateHandler.state.value.articlePageSnapshot
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
        stateHandler.setArticlesLoading(reset = page.value == 1)

        getArticleBehaviour.load(
            snapshot = stateHandler.currentArticlePageSnapshot(),
            sourceId = stateHandler.state.value.selectedSource?.id,
            page = page,
        ).onSuccess(stateHandler::updateArticlePageSnapshot)
            .onFailure { error ->
                stateHandler.setArticlesError(error.message)
            }
    }
}
