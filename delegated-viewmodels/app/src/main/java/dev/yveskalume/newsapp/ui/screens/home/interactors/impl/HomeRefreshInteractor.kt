package dev.yveskalume.newsapp.ui.screens.home.interactors.impl

import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.home.HomeStateHandler
import dev.yveskalume.newsapp.ui.screens.home.interactors.RefreshInteractor
import dev.yveskalume.newsapp.util.paging.PageNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeRefreshInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: HomeStateHandler,
    private val sourcesRepository: SourcesRepository,
    private val getArticleBehaviour: GetArticleBehaviour,
) : RefreshInteractor {
    private var refreshJob: Job? = null

    override fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            stateHandler.setRefreshLoading(true)

            try {
                sourcesRepository.getSources()
                    .onSuccess(stateHandler::setSourcesSuccess)
                    .onFailure { error ->
                        stateHandler.setSourcesError(error.message)
                    }

                val firstPage = PageNumber(1)
                stateHandler.setArticlesLoading(reset = true)

                getArticleBehaviour.load(
                    snapshot = stateHandler.currentArticlePageSnapshot(),
                    sourceId = stateHandler.state.value.selectedSource?.id,
                    page = firstPage,
                ).onSuccess(stateHandler::updateArticlePageSnapshot)
                    .onFailure { error ->
                        stateHandler.setArticlesError(error.message)
                    }
            } finally {
                stateHandler.setRefreshLoading(false)
            }
        }
    }
}
