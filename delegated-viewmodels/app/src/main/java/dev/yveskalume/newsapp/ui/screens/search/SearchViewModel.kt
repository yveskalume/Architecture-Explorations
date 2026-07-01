package dev.yveskalume.newsapp.ui.screens.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel(
    stateHandler: SearchStateHandler,
    val interactors: SearchInteractors,
) : ViewModel() {
    val uiState: StateFlow<SearchUiState> = stateHandler.state
}
