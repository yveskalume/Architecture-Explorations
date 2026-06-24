package dev.yveskalume.newsappp.ui.screens.home.di

import dev.yveskalume.newsappp.core.StateManager
import dev.yveskalume.newsappp.core.getStateManager
import dev.yveskalume.newsappp.core.scopedReducer
import dev.yveskalume.newsappp.ui.screens.home.HomeEvent
import dev.yveskalume.newsappp.ui.screens.home.HomeUiState
import dev.yveskalume.newsappp.ui.screens.home.HomeViewModel
import dev.yveskalume.newsappp.ui.screens.home.reducers.LoadArticlesReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.LoadMoreReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.LoadSourcesReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.RefreshReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.SelectSourceReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.SetPagingLoadingReducer
import dev.yveskalume.newsappp.ui.screens.home.reducers.SetRefreshLoadingReducer
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val HomeDiContainer = "Home_DI_Container"

val HomeDiModule = module {
    scope(named(HomeDiContainer)) {

        viewModelOf(::HomeViewModel)

        scoped<StateManager<*, *>> {
            StateManager<HomeUiState, HomeEvent>(
                scope = this@scoped,
                initialState = HomeUiState.initial()
            )
        }

        scopedReducer<HomeUiState, HomeEvent.LoadArticles> {
            LoadArticlesReducer(get())
        }

        scopedReducer<HomeUiState, HomeEvent.LoadSources> {
            LoadSourcesReducer(get())
        }

        scopedReducer<HomeUiState, HomeEvent.SelectSource> {
            SelectSourceReducer(
                onPublishEvent = { event ->
                    getStateManager<HomeUiState, HomeEvent>()?.onEvent(event)
                },
            )
        }

        scopedReducer<HomeUiState, HomeEvent.Refresh> {
            RefreshReducer(
                onPublishEvent = { event ->
                    getStateManager<HomeUiState, HomeEvent>()?.onEvent(event)
                },
            )
        }

        scopedReducer<HomeUiState, HomeEvent.LoadMore> {
            LoadMoreReducer(
                onPublishEvent = { event ->
                    getStateManager<HomeUiState, HomeEvent>()?.onEvent(event)
                },
            )
        }

        scopedReducer<HomeUiState, HomeEvent.SetRefreshLoading> {
            SetRefreshLoadingReducer()
        }

        scopedReducer<HomeUiState, HomeEvent.SetPagingLoading> {
            SetPagingLoadingReducer()
        }
    }
}
