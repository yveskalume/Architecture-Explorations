package dev.yveskalume.newsapp.ui.screens.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel(
    stateStore: SearchStateStore,
    val controllers: SearchControllers,
) : ViewModel() {
    val uiState: StateFlow<SearchUiState> = stateStore.state
}
