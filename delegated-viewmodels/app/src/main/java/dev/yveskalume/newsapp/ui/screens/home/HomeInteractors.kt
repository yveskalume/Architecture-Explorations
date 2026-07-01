package dev.yveskalume.newsapp.ui.screens.home

import dev.yveskalume.newsapp.ui.screens.home.interactors.ArticlesInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.RefreshInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.SourcesInteractor

class HomeInteractors(
    sourcesInteractor: SourcesInteractor,
    articlesInteractor: ArticlesInteractor,
    refreshInteractor: RefreshInteractor,
) : SourcesInteractor by sourcesInteractor,
    ArticlesInteractor by articlesInteractor,
    RefreshInteractor by refreshInteractor
