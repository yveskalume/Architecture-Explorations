package dev.yveskalume.newsapp.ui.screens.home.controllers.impl

import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.home.HomeStateStore
import dev.yveskalume.newsapp.ui.screens.home.controllers.SourcesController
import dev.yveskalume.newsapp.util.paging.PageNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeSourcesController(
    private val scope: CoroutineScope,
    private val stateStore: HomeStateStore,
    private val getArticleLogic: GetArticleLogic,
) : SourcesController {
    private var selectSourceJob: Job? = null

    override fun selectSource(source: SourceItem?) {
        val currentSource = stateStore.state.value.selectedSource
        val selectedSource = source.takeIf {
            it?.id != currentSource?.id
        }
        stateStore.updateSourcesSelection(selectedSource)
        selectSourceJob?.cancel()
        selectSourceJob = scope.launch {
            val firstPage = PageNumber(1)
            stateStore.setArticlesLoading(reset = true)

            getArticleLogic.load(
                snapshot = stateStore.currentArticlePageSnapshot(),
                sourceId = selectedSource?.id,
                page = firstPage,
            ).onSuccess(stateStore::updateArticlePageSnapshot)
                .onFailure { error ->
                    stateStore.setArticlesError(error.message)
                }
        }
    }
}
