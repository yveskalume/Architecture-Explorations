package dev.yveskalume.newsappp.ui.screens.home.reducers

import dev.yveskalume.newsappp.core.Reducer
import dev.yveskalume.newsappp.ui.screens.home.HomeEvent
import dev.yveskalume.newsappp.ui.screens.home.HomeUiState
import dev.yveskalume.newsappp.ui.screens.home.SourcesUiState

class SelectSourceReducer(
    private val onPublishEvent: (HomeEvent) -> Unit = {},
) : Reducer<HomeUiState, HomeEvent.SelectSource> {
    override suspend fun reduce(
        state: HomeUiState,
        event: HomeEvent.SelectSource
    ): HomeUiState {
        val selectedSource = event.source.takeIf { it?.id != state.selectedSource?.id }
        val sourcesUiState = when (val currentSourcesUiState = state.sourcesUiState) {
            is SourcesUiState.Success -> currentSourcesUiState.copy(selected = selectedSource)
            else -> currentSourcesUiState
        }

        onPublishEvent(HomeEvent.LoadArticles())

        return state.copy(
            selectedSource = selectedSource,
            sourcesUiState = sourcesUiState
        )
    }
}
