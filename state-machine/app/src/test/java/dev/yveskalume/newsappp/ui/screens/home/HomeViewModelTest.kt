package dev.yveskalume.newsappp.ui.screens.home

import dev.yveskalume.newsappp.core.StateManager
import dev.yveskalume.newsappp.core.getStateManager
import dev.yveskalume.newsappp.core.scopedReducer
import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsappp.fake.FakeArticleRepositorySuccess
import dev.yveskalume.newsappp.fake.FakeSourcesRepositorySuccess
import dev.yveskalume.newsappp.ui.screens.home.reducers.LoadArticlesReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.LoadMoreReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.LoadSourcesReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.RefreshReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.SelectSourceReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.SetPagingLoadingReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.SetRefreshLoadingReducer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.StandardTestDispatcher
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("successful data load case") {
        When("state collection starts") {
            Then("it should load sources and articles") {
                runTest(testDispatcher) {
                    val testScope = createHomeScope(
                        articleRepository = FakeArticleRepositorySuccess(),
                        sourcesRepository = FakeSourcesRepositorySuccess()
                    )

                    try {
                        val viewModel = HomeViewModel(testScope.stateManager)
                        val collectJob = launch { viewModel.uiState.collect() }

                        advanceUntilIdle()

                        val state = viewModel.uiState.value
                        state.sourcesUiState.shouldBeInstanceOf<SourcesUiState.Success>()
                        state.articleUiState.shouldBeInstanceOf<ArticleUiState.Success>()

                        collectJob.cancel()
                    } finally {
                        testScope.close()
                    }
                }
            }
        }

        When("a source is selected") {
            Then("it should update selectedSource") {
                runTest(testDispatcher) {
                    val testScope = createHomeScope(
                        articleRepository = FakeArticleRepositorySuccess(),
                        sourcesRepository = FakeSourcesRepositorySuccess()
                    )

                    try {
                        val viewModel = HomeViewModel(testScope.stateManager)
                        val collectJob = launch { viewModel.uiState.collect() }

                        advanceUntilIdle()

                        val source = FakeSourcesRepositorySuccess.sampleSources.first()
                        viewModel.onEvent(HomeEvent.SelectSource(source))
                        advanceUntilIdle()

                        viewModel.uiState.value.selectedSource shouldBe source
                        (viewModel.uiState.value.sourcesUiState as SourcesUiState.Success).selected shouldBe source

                        collectJob.cancel()
                    } finally {
                        testScope.close()
                    }
                }
            }
        }
    }
})

private data class HomeTestScope(
    val koinApp: KoinApplication,
    val scope: Scope,
    val stateManager: StateManager<HomeUiState, HomeEvent>
) {
    fun close() {
        scope.close()
        koinApp.close()
    }
}

private fun createHomeScope(
    articleRepository: ArticleRepository,
    sourcesRepository: SourcesRepository
): HomeTestScope {
    val module = module {
        scope(named("HomeTestScope")) {
            scoped<ArticleRepository> { articleRepository }
            scoped<SourcesRepository> { sourcesRepository }

            scoped<StateManager<*, *>> {
                StateManager<HomeUiState, HomeEvent>(
                    scope = this@scoped,
                    initialState = HomeUiState.initial()
                )
            }

            scopedReducer<HomeUiState, HomeEvent.LoadArticles> { LoadArticlesReducer(get()) }
            scopedReducer<HomeUiState, HomeEvent.LoadSources> { LoadSourcesReducer(get()) }
            scopedReducer<HomeUiState, HomeEvent.SelectSource> {
                SelectSourceReducer { event ->
                    getStateManager<HomeUiState, HomeEvent>()?.onEvent(event)
                }
            }
            scopedReducer<HomeUiState, HomeEvent.SetPagingLoading> { SetPagingLoadingReducer() }
            scopedReducer<HomeUiState, HomeEvent.SetRefreshLoading> { SetRefreshLoadingReducer() }
            scopedReducer<HomeUiState, HomeEvent.Refresh> {
                RefreshReducer { event ->
                    getStateManager<HomeUiState, HomeEvent>()?.onEvent(event)
                }
            }
            scopedReducer<HomeUiState, HomeEvent.LoadMore> {
                LoadMoreReducer { event ->
                    getStateManager<HomeUiState, HomeEvent>()?.onEvent(event)
                }
            }
        }
    }

    val koinApp = koinApplication { modules(module) }
    val scope = koinApp.koin.createScope("home-test", named("HomeTestScope"))
    val stateManager = scope.get<StateManager<HomeUiState, HomeEvent>>()

    return HomeTestScope(koinApp, scope, stateManager)
}
