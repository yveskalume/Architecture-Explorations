package dev.yveskalume.newsapp.ui.screens.home.controllers.impl

import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.home.HomeStateStore
import dev.yveskalume.newsapp.ui.screens.home.controllers.RefreshController
import dev.yveskalume.newsapp.util.paging.PageNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeRefreshController(
    private val scope: CoroutineScope,
    private val stateStore: HomeStateStore,
    private val sourcesRepository: SourcesRepository,
    private val getArticleLogic: GetArticleLogic,
) : RefreshController {
    private var refreshJob: Job? = null

    override fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            stateStore.setRefreshLoading(true)

            try {
                sourcesRepository.getSources()
                    .onSuccess(stateStore::setSourcesSuccess)
                    .onFailure { error ->
                        stateStore.setSourcesError(error.message)
                    }

                val firstPage = PageNumber(1)
                stateStore.setArticlesLoading(reset = true)

                getArticleLogic.load(
                    snapshot = stateStore.currentArticlePageSnapshot(),
                    sourceId = stateStore.state.value.selectedSource?.id,
                    page = firstPage,
                ).onSuccess(stateStore::updateArticlePageSnapshot)
                    .onFailure { error ->
                        stateStore.setArticlesError(error.message)
                    }
            } finally {
                stateStore.setRefreshLoading(false)
            }
        }
    }
}
