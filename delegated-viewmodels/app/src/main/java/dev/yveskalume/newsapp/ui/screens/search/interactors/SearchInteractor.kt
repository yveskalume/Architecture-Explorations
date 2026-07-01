package dev.yveskalume.newsapp.ui.screens.search.interactors

interface SearchInteractor {
    fun onQueryChanged(query: String)
    fun clearSearch()
    fun loadMore()
}
