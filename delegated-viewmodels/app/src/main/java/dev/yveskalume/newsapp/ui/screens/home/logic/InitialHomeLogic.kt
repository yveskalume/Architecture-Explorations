package dev.yveskalume.newsapp.ui.screens.home.logic

import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.home.HomeStateStore
import dev.yveskalume.newsapp.util.paging.PageNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class InitialHomeLogic(
    private val sourcesRepository: SourcesRepository,
    private val stateStore: HomeStateStore,
    private val getArticleLogic: GetArticleLogic,
) {
    private var hasStarted = false

    suspend fun load() {
        coroutineScope {
            stateStore.setSourcesLoading()

            val sourcesRequest = async {
                sourcesRepository.getSources()
            }

            val articlesRequest = async {
                loadArticles(PageNumber(1))
            }

            sourcesRequest.await()
                .onSuccess(stateStore::setSourcesSuccess)
                .onFailure { error ->
                    stateStore.setSourcesError(error.message)
                }

            articlesRequest.await()
        }
    }

    private suspend fun loadArticles(page: PageNumber) {
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
