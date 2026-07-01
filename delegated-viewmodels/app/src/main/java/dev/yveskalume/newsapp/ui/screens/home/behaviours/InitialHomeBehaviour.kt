package dev.yveskalume.newsapp.ui.screens.home.behaviours

import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.home.HomeStateHandler
import dev.yveskalume.newsapp.util.paging.PageNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class InitialHomeBehaviour(
    private val sourcesRepository: SourcesRepository,
    private val stateHandler: HomeStateHandler,
    private val getArticleBehaviour: GetArticleBehaviour,
) {
    private var hasStarted = false

    suspend fun load() {
        coroutineScope {
            stateHandler.setSourcesLoading()

            val sourcesRequest = async {
                sourcesRepository.getSources()
            }

            val articlesRequest = async {
                loadArticles(PageNumber(1))
            }

            sourcesRequest.await()
                .onSuccess(stateHandler::setSourcesSuccess)
                .onFailure { error ->
                    stateHandler.setSourcesError(error.message)
                }

            articlesRequest.await()
        }
    }

    private suspend fun loadArticles(page: PageNumber) {
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
