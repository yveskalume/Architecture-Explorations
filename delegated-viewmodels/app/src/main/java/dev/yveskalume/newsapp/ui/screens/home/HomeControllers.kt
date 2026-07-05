package dev.yveskalume.newsapp.ui.screens.home

import dev.yveskalume.newsapp.ui.screens.home.controllers.ArticlesController
import dev.yveskalume.newsapp.ui.screens.home.controllers.RefreshController
import dev.yveskalume.newsapp.ui.screens.home.controllers.SourcesController

class HomeControllers(
    sourcesController: SourcesController,
    articlesController: ArticlesController,
    refreshController: RefreshController,
) : SourcesController by sourcesController,
    ArticlesController by articlesController,
    RefreshController by refreshController
