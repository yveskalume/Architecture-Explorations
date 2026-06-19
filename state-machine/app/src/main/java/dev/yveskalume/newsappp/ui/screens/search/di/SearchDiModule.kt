package dev.yveskalume.newsappp.ui.screens.search.di

import dev.yveskalume.newsappp.core.StateManager
import dev.yveskalume.newsappp.core.getStateManager
import dev.yveskalume.newsappp.core.scopedReducer
import dev.yveskalume.newsappp.ui.screens.search.SearchEvent
import dev.yveskalume.newsappp.ui.screens.search.SearchUiState
import dev.yveskalume.newsappp.ui.screens.search.SearchViewModel
import dev.yveskalume.newsappp.ui.screens.search.reducers.ClearSearchReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SearchLoadArticlesReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SearchLoadMoreReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SearchQueryChangedReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.PerformSearchReducer
import dev.yveskalume.newsappp.ui.screens.search.reducers.SetPagingLoadingReducer
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.core.scope.get
import org.koin.dsl.module

internal const val SearchDiContainer = "Search_DI_Container"

val SearchDiModule = module {
    scope(named(SearchDiContainer)) {

        viewModelOf(::SearchViewModel)

        scoped<StateManager<*, *>> {
            StateManager<SearchUiState, SearchEvent>(
                scope = this@scoped,
                initialState = SearchUiState.initial()
            )
        }

        scopedReducer<SearchUiState, SearchEvent.QueryChanged> {
            SearchQueryChangedReducer(
                coroutineScope = get<CoroutineScope>(),
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

        scopedReducer<SearchUiState, SearchEvent.LoadArticles> {
            SearchLoadArticlesReducer(get())
        }

        scopedReducer<SearchUiState, SearchEvent.LoadMore> {
            SearchLoadMoreReducer(
                onPublishEvent = { event ->
                    getStateManager<SearchUiState, SearchEvent>()?.onEvent(event)
                }
            )
        }

        scopedReducer<SearchUiState, SearchEvent.SetPagingLoading> {
            SetPagingLoadingReducer()
        }

        scopedReducer<SearchUiState, SearchEvent.ClearSearch> {
            ClearSearchReducer()
        }
    }
}

