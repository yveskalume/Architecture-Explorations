package dev.yveskalume.newsapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yveskalume.newsapp.ui.screens.home.behaviours.InitialHomeBehaviour
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val stateHandler: HomeStateHandler,
    val interactors: HomeInteractors,
    private val initialHomeBehaviour: InitialHomeBehaviour,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = stateHandler.state
        .onStart {
            initialHomeBehaviour.load()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.initial(),
        )
}
