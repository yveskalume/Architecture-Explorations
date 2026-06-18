package dev.yveskalume.newsappp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsappp.util.RestartableStateFlow
import dev.yveskalume.newsappp.util.paging.Pager
import dev.yveskalume.newsappp.util.paging.createPage
import dev.yveskalume.newsappp.util.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

interface IHomeViewModel {
    fun selectSource(source: SourceItem?)
    fun refresh()
    fun loadMore()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val sourcesRepository: SourcesRepository
) : ViewModel(), IHomeViewModel {

    private val selectedSourceFlow = MutableStateFlow<SourceItem?>(null)
    private val sourcesStateFlow = MutableStateFlow<List<SourceItem>>(emptyList())


    val refreshUiState: StateFlow<RefreshUiState>
        field = MutableStateFlow<RefreshUiState>(RefreshUiState.Idle)

    val sourcesUiState: RestartableStateFlow<SourcesUiState> = combine(
        sourcesStateFlow,
        selectedSourceFlow
    ) { sources, selectedSource ->
        SourcesUiState.Success(sources = sources, selected = selectedSource)
    }.onStart {
        sourcesStateFlow.update { sourcesRepository.getSources().getOrThrow() }
    }.catch<SourcesUiState> {
        emit(SourcesUiState.Error(it.message ?: "Failed to load sources"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SourcesUiState.Loading
    )


    val articlePager: Pager<Article> = createPage(viewModelScope) {
        articleRepository.getTopHeadlines(
            sources = selectedSourceFlow.value?.id,
            pageSize = PAGE_SIZE,
            page = it.value
        )
    }.also {
        resetRefreshState(it, refreshUiState)
    }

    private fun resetRefreshState(
        pager: Pager<Article>,
        refreshUiState: MutableStateFlow<RefreshUiState>
    ) {
        viewModelScope.launch {
            pager.snapshot.onEach { s ->
                if (!s.dataState.isLoading()) {
                    refreshUiState.update { RefreshUiState.Idle }
                }
            }.collect()
        }
    }


    override fun selectSource(source: SourceItem?) {
        selectedSourceFlow.update { source.takeIf { it?.id != selectedSourceFlow.value?.id } }
        articlePager.retry()
    }

    override fun refresh() {
        refreshUiState.update { RefreshUiState.Refreshing }
        sourcesUiState.restart()
        articlePager.refresh()
    }

    override fun loadMore() {
        articlePager.loadMore()
    }
}
