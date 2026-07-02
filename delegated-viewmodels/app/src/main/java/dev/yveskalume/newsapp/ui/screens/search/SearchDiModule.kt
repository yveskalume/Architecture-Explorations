package dev.yveskalume.newsapp.ui.screens.search

import dev.yveskalume.newsapp.ui.logic.GetArticleLogic
import dev.yveskalume.newsapp.ui.screens.search.logic.SearchLogic
import dev.yveskalume.newsapp.ui.screens.search.controllers.SearchController
import dev.yveskalume.newsapp.ui.screens.search.controllers.SearchScreenController
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
val searchModule = module {
    viewModelScope {
        scoped<CoroutineScope> {
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        } withOptions {
            onClose { scope ->
                scope?.cancel()
            }
        }

        scoped {
            SearchStateStore()
        }

        scoped {
            SearchLogic()
        }

        scoped {
            GetArticleLogic(
                articleRepository = get(),
            )
        }

        scoped<SearchController> {
            SearchScreenController(
                scope = get(),
                stateStore = get(),
                getArticleLogic = get(),
                searchLogic = get(),
            )
        }

        scoped {
            SearchControllers(searchController = get())
        }

        viewModel {
            SearchViewModel(
                stateStore = get(),
                controllers = get(),
            )
        }
    }
}
