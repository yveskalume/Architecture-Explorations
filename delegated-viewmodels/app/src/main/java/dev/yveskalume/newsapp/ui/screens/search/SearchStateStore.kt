package dev.yveskalume.newsapp.ui.screens.search

import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageSnapshot
import dev.yveskalume.newsapp.util.paging.PageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchStateStore {
    private val _state = MutableStateFlow(SearchUiState.initial())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun update(reducer: SearchUiState.() -> SearchUiState) {
        _state.update { current -> current.reducer() }
    }

    fun setQuery(query: String) {
        update { copy(query = query) }
    }

    fun clearArticles() {
        update {
            copy(
                articlePageSnapshot = PageSnapshot(
                    dataState = DataState.Success<Article>(emptyList()),
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
                    dataState = DataState.Error(message ?: "Failed to search news"),
                )
            }
            copy(articlePageSnapshot = updatedSnapshot)
        }
    }
}
