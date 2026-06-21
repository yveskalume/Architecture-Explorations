package dev.yveskalume.newsappp.ui.screens.home.reducers

import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsappp.core.Reducer
import dev.yveskalume.newsappp.ui.screens.home.HomeEvent
import dev.yveskalume.newsappp.ui.screens.home.HomeUiState
import dev.yveskalume.newsappp.ui.screens.home.SourcesUiState

class LoadSourcesReducer(
    private val sourcesRepository: SourcesRepository
) : Reducer<HomeUiState, HomeEvent.LoadSources> {
    override suspend fun reduce(
        state: HomeUiState,
        event: HomeEvent.LoadSources
    ): HomeUiState {
        val newState = sourcesRepository.getSources().fold(
            onSuccess = { sources ->
                SourcesUiState.Success(sources)
            },
            onFailure = {
                SourcesUiState.Error(message = it.message ?: "Failed to load sources")
            }
        )
        return state.copy(sourcesUiState = newState)
    }
}