package dev.yveskalume.newsapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yveskalume.newsapp.ui.screens.home.logic.InitialHomeLogic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val stateStore: HomeStateStore,
    val controllers: HomeControllers,
    private val initialHomeLogic: InitialHomeLogic,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = stateStore.state
        .onStart {
            initialHomeLogic.load()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.initial(),
        )
}
