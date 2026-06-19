package dev.yveskalume.newsappp.ui.screens.home

import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsappp.core.Event

sealed interface HomeEvent : Event {
    data class LoadArticles(val page: Int = 1) : HomeEvent
    data object LoadSources : HomeEvent
    data class SelectSource(val source: SourceItem?) : HomeEvent
    data object Refresh : HomeEvent
    data object LoadMore : HomeEvent

    /** System event **/
    data object SetPagingLoading : HomeEvent
    data class SetRefreshLoading(val isLoading: Boolean) : HomeEvent

}