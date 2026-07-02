package dev.yveskalume.newsapp.ui.screens.home

import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.home.logic.InitialHomeLogic
import dev.yveskalume.newsapp.ui.screens.home.controllers.ArticlesController
import dev.yveskalume.newsapp.ui.screens.home.controllers.impl.HomeArticlesController
import dev.yveskalume.newsapp.ui.screens.home.controllers.impl.HomeRefreshController
import dev.yveskalume.newsapp.ui.screens.home.controllers.impl.HomeSourcesController
import dev.yveskalume.newsapp.ui.screens.home.controllers.RefreshController
import dev.yveskalume.newsapp.ui.screens.home.controllers.SourcesController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinViewModelScopeApi
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.koin.viewmodel.scope.viewModelScope

@OptIn(KoinExperimentalAPI::class, KoinViewModelScopeApi::class)
val homeModule = module {
    viewModelScope {
        scoped<CoroutineScope> {
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        } withOptions {
            onClose { scope ->
                scope?.cancel()
            }
        }

        viewModel {
            HomeViewModel(
                stateStore = get(),
                controllers = get(),
                initialHomeLogic = get(),
            )
        }

        scoped {
            HomeStateStore()
        }

        scoped {
            InitialHomeLogic(
                sourcesRepository = get(),
                stateStore = get(),
                getArticleLogic = get(),
            )
        }

        scoped {
            GetArticleLogic(
                articleRepository = get(),
            )
        }

        scoped<SourcesController> {
            HomeSourcesController(
                scope = get(),
                stateStore = get(),
                getArticleLogic = get(),
            )
        }

        scoped<ArticlesController> {
            HomeArticlesController(
                scope = get(),
                stateStore = get(),
                getArticleLogic = get(),
            )
        }

        scoped<RefreshController> {
            HomeRefreshController(
                scope = get(),
                stateStore = get(),
                sourcesRepository = get(),
                getArticleLogic = get(),
            )
        }

        scoped {
            HomeControllers(
                sourcesController = get(),
                articlesController = get(),
                refreshController = get(),
            )
        }


    }
}
