package dev.yveskalume.newsapp.ui.screens.search.controllers

interface SearchController {
    fun onQueryChanged(query: String)
    fun clearSearch()
    fun loadMore()
}
