package dev.yveskalume.newsappp.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.util.paging.Pager
import dev.yveskalume.newsappp.util.paging.createPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val PAGE_SIZE = 20

interface ISearchViewModel {
    fun onQueryChange(query: String)
    fun clearSearch()
    fun loadMore()
}

class SearchViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel(), ISearchViewModel {

    val queryUiState: StateFlow<String>
        field = MutableStateFlow("")

    val articlePager: Pager<Article> = createPage(viewModelScope) { page ->
        val query = queryUiState.value.trim()
        if (query.isBlank()) {
            Result.success(emptyList())
        } else {
            articleRepository.getTopHeadlines(
                query = query,
                pageSize = PAGE_SIZE,
                page = page.value
            )
        }
    }

    override fun onQueryChange(query: String) {
        queryUiState.value = query
        articlePager.retry()
    }

    override fun clearSearch() {
        queryUiState.value = ""
        articlePager.retry()
    }

    override fun loadMore() {
        if (queryUiState.value.isBlank()) return
        articlePager.loadMore()
    }
}
