package dev.yveskalume.newsappp.ui.screens.home.reducers

import dev.yveskalume.newsappp.core.Reducer
import dev.yveskalume.newsappp.ui.screens.home.HomeEvent
import dev.yveskalume.newsappp.ui.screens.home.HomeUiState

class RefreshReducer (
    private val onPublishEvent: (HomeEvent) -> Unit,
) : Reducer<HomeUiState, HomeEvent.Refresh> {
    
    override suspend fun reduce(
        state: HomeUiState,
        event: HomeEvent.Refresh
    ): HomeUiState {

        onPublishEvent(HomeEvent.SetRefreshLoading(true))

        onPublishEvent(HomeEvent.LoadArticles())

        onPublishEvent(HomeEvent.SetRefreshLoading(false))

        return state
    }
}