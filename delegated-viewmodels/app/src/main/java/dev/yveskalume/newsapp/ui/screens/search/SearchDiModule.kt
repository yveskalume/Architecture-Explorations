package dev.yveskalume.newsapp.ui.screens.search

import dev.yveskalume.newsapp.ui.behaviours.GetArticleBehaviour
import dev.yveskalume.newsapp.ui.screens.search.behaviours.SearchBehaviour
import dev.yveskalume.newsapp.ui.screens.search.interactors.SearchInteractor
import dev.yveskalume.newsapp.ui.screens.search.interactors.SearchScreenInteractor
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
            SearchStateHandler()
        }

        scoped {
            SearchBehaviour()
        }

        scoped {
            GetArticleBehaviour(
                articleRepository = get(),
            )
        }

        scoped<SearchInteractor> {
            SearchScreenInteractor(
                scope = get(),
                stateHandler = get(),
                getArticleBehaviour = get(),
                searchBehaviour = get(),
            )
        }

        scoped {
            SearchInteractors(searchInteractor = get())
        }

        viewModel {
            SearchViewModel(
                stateHandler = get(),
                interactors = get(),
            )
        }
    }
}
