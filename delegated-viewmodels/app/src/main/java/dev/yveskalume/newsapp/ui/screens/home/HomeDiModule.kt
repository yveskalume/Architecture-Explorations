package dev.yveskalume.newsapp.ui.screens.home

import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.home.behaviours.InitialHomeBehaviour
import dev.yveskalume.newsapp.ui.screens.home.interactors.ArticlesInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.impl.HomeArticlesInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.impl.HomeRefreshInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.impl.HomeSourcesInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.RefreshInteractor
import dev.yveskalume.newsapp.ui.screens.home.interactors.SourcesInteractor
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
                stateHandler = get(),
                interactors = get(),
                initialHomeBehaviour = get(),
            )
        }

        scoped {
            HomeStateHandler()
        }

        scoped {
            InitialHomeBehaviour(
                sourcesRepository = get(),
                stateHandler = get(),
                getArticleBehaviour = get(),
            )
        }

        scoped {
            GetArticleBehaviour(
                articleRepository = get(),
            )
        }

        scoped<SourcesInteractor> {
            HomeSourcesInteractor(
                scope = get(),
                stateHandler = get(),
                getArticleBehaviour = get(),
            )
        }

        scoped<ArticlesInteractor> {
            HomeArticlesInteractor(
                scope = get(),
                stateHandler = get(),
                getArticleBehaviour = get(),
            )
        }

        scoped<RefreshInteractor> {
            HomeRefreshInteractor(
                scope = get(),
                stateHandler = get(),
                sourcesRepository = get(),
                getArticleBehaviour = get(),
            )
        }

        scoped {
            HomeInteractors(
                sourcesInteractor = get(),
                articlesInteractor = get(),
                refreshInteractor = get(),
            )
        }


    }
}
