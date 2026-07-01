package dev.yveskalume.newsapp.ui.screens.home.interactors.impl

import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.home.HomeStateHandler
import dev.yveskalume.newsapp.ui.screens.home.interactors.SourcesInteractor
import dev.yveskalume.newsapp.util.paging.PageNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeSourcesInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: HomeStateHandler,
    private val getArticleBehaviour: GetArticleBehaviour,
) : SourcesInteractor {
    private var selectSourceJob: Job? = null

    override fun selectSource(source: SourceItem?) {
        val currentSource = stateHandler.state.value.selectedSource
        val selectedSource = source.takeIf {
            it?.id != currentSource?.id
        }
        stateHandler.updateSourcesSelection(selectedSource)
        selectSourceJob?.cancel()
        selectSourceJob = scope.launch {
            val firstPage = PageNumber(1)
            stateHandler.setArticlesLoading(reset = true)

            getArticleBehaviour.load(
                snapshot = stateHandler.currentArticlePageSnapshot(),
                sourceId = selectedSource?.id,
                page = firstPage,
            ).onSuccess(stateHandler::updateArticlePageSnapshot)
                .onFailure { error ->
                    stateHandler.setArticlesError(error.message)
                }
        }
    }
}
