package dev.yveskalume.newsapp.ui.screens.search

import dev.yveskalume.newsapp.ui.screens.search.interactors.SearchInteractor

class SearchInteractors(
    searchInteractor: SearchInteractor,
) : SearchInteractor by searchInteractor
