package dev.yveskalume.newsappp.ui.screens.search

import dev.yveskalume.newsappp.core.StateManager
import dev.yveskalume.newsappp.core.getStateManager
import dev.yveskalume.newsappp.core.scopedReducer
import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsappp.fake.FakeArticleRepositorySuccess
import dev.yveskalume.newsappp.ui.screens.search.reducers.ClearSearchReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.PerformSearchReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SearchLoadArticlesReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SearchLoadMoreReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SearchQueryChangedReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SetPagingLoadingReducer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest : BehaviorSpec({
    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("a search view model configured with a success article repository") {
        When("the query changes to a non-empty value") {
            Then("it should load search results") {
                runTest(testDispatcher) {
                    val testScope = createSearchScope(
                        articleRepository = FakeArticleRepositorySuccess(),
                        coroutineScope = this
                    )

                    try {
                        val viewModel = SearchViewModel(testScope.stateManager)
                        val collectJob = launch { viewModel.uiState.collect() }

                        advanceUntilIdle()

                        viewModel.onEvent(SearchEvent.QueryChanged("kotlin"))
                        advanceUntilIdle()

                        val state = viewModel.uiState.value
                        state.query shouldBe "kotlin"
                        state.searchResultUiState.shouldBeInstanceOf<SearchResultUiState.Success>()

                        collectJob.cancel()
                    } finally {
                        testScope.close()
                    }
                }
            }
        }
    }
})

private data class SearchTestScope(
    val koinApp: KoinApplication,
    val scope: Scope,
    val stateManager: StateManager<SearchUiState, SearchEvent>
) {
    fun close() {
        scope.close()
        koinApp.close()
    }
}

private fun createSearchScope(
    articleRepository: ArticleRepository,
    coroutineScope: CoroutineScope
): SearchTestScope {
    val module = module {
        scope(named("SearchTestScope")) {
            scoped<ArticleRepository> { articleRepository }
            scoped<CoroutineScope> { coroutineScope }

            scoped<StateManager<*, *>> {
                StateManager<SearchUiState, SearchEvent>(
                    scope = this@scoped,
                    initialState = SearchUiState.initial()
                )
            }

            scopedReducer<SearchUiState, SearchEvent.QueryChanged> {
                SearchQueryChangedReducer(
                    coroutineScope = get(),
                    onPublishEvent = { event ->
                        getStateManager<SearchUiState, SearchEvent>()?.onEvent(event)
                    }
                )
            }

            scopedReducer<SearchUiState, SearchEvent.PerformSearch> {
                PerformSearchReducer(
                    onPublishEvent = { event ->
                        getStateManager<SearchUiState, SearchEvent>()?.onEvent(event)
                    }
                )
            }

            scopedReducer<SearchUiState, SearchEvent.LoadArticles> { SearchLoadArticlesReducer(get()) }
            scopedReducer<SearchUiState, SearchEvent.LoadMore> {
                SearchLoadMoreReducer { event ->
                    getStateManager<SearchUiState, SearchEvent>()?.onEvent(event)
                }
            }
            scopedReducer<SearchUiState, SearchEvent.SetPagingLoading> { SetPagingLoadingReducer() }
            scopedReducer<SearchUiState, SearchEvent.ClearSearch> { ClearSearchReducer() }
        }
    }

    val koinApp = koinApplication { modules(module) }
    val scope = koinApp.koin.createScope("search-test", named("SearchTestScope"))
    val stateManager = scope.get<StateManager<SearchUiState, SearchEvent>>()

    return SearchTestScope(koinApp, scope, stateManager)
}
