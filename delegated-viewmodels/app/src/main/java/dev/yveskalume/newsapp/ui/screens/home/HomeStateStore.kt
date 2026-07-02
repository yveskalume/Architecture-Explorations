package dev.yveskalume.newsapp.ui.screens.home

import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageSnapshot
import dev.yveskalume.newsapp.util.paging.PageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeStateStore {
    private val _state = MutableStateFlow(HomeUiState.initial())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun update(reducer: HomeUiState.() -> HomeUiState) {
        _state.update { current ->
            current.reducer()
        }
    }

    fun updateSourcesSelection(source: SourceItem?) {
        update {
            val updatedSourcesUiState = when (val current = sourcesUiState) {
                is SourcesUiState.Success -> current.copy(selected = source)
                else -> current
            }

            copy(
                selectedSource = source,
                sourcesUiState = updatedSourcesUiState,
            )
        }
    }

    fun setSourcesLoading() {
        update {
            copy(sourcesUiState = SourcesUiState.Loading)
        }
    }

    fun setSourcesSuccess(sources: List<SourceItem>) {
        update {
            copy(
                sourcesUiState = SourcesUiState.Success(
                    sources = sources,
                    selected = selectedSource,
                )
            )
        }
    }

    fun setSourcesError(message: String?) {
        update {
            copy(
                sourcesUiState = SourcesUiState.Error(
                    message = message ?: "Failed to load sources",
                )
            )
        }
    }

    fun currentArticlePageSnapshot(): PageSnapshot<Article> {
        return state.value.articlePageSnapshot
    }

    fun updateArticlePageSnapshot(snapshot: PageSnapshot<Article>) {
        update {
            copy(articlePageSnapshot = snapshot)
        }
    }

    fun setArticlesLoading(reset: Boolean) {
        update {
            val hasArticles = articlePageSnapshot.dataState is DataState.Success
            copy(
                articlePageSnapshot = if (hasArticles && !reset) {
                    articlePageSnapshot.copy(pageState = PageState.Loading)
                } else {
                    articlePageSnapshot.copy(
                        pageState = PageState.Idle,
                        dataState = DataState.Loading,
                    )
                }
            )
        }
    }

    fun setArticlesError(message: String?) {
        update {
            val hasArticles = articlePageSnapshot.dataState is DataState.Success
            val updatedSnapshot = if (hasArticles) {
                articlePageSnapshot.copy(pageState = PageState.Idle)
            } else {
                articlePageSnapshot.copy(
                    pageState = PageState.Idle,
                    dataState = DataState.Error(message ?: "Failed to load news"),
                )
            }
            copy(articlePageSnapshot = updatedSnapshot)
        }
    }

    fun setRefreshLoading(isLoading: Boolean) {
        update {
            copy(
                refreshUiState = if (isLoading) {
                    RefreshUiState.Refreshing
                } else {
                    RefreshUiState.Idle
                }
            )
        }
    }
}
