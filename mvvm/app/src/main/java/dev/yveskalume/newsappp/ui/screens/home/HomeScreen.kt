package dev.yveskalume.newsappp.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsapp.ui.components.NewsCard
import dev.yveskalume.newsapp.ui.components.NewsCardShimmer
import dev.yveskalume.newsapp.ui.components.SourcesRow
import dev.yveskalume.newsapp.ui.components.SourcesRowShimmer
import dev.yveskalume.newsappp.R
import dev.yveskalume.newsapp.ui.components.EmptyContent
import dev.yveskalume.newsapp.ui.components.ErrorContent
import dev.yveskalume.newsappp.ui.preview.HomeScreenPreviewData
import dev.yveskalume.newsappp.ui.preview.HomeScreenPreviewProvider
import dev.yveskalume.newsappp.ui.screens.home.components.TopAppBar
import dev.yveskalume.newsappp.ui.theme.NewsApppTheme
import dev.yveskalume.newsappp.util.paddingAndConsumeWindowInsets
import dev.yveskalume.newsappp.util.paging.DataState
import dev.yveskalume.newsappp.util.paging.LazyPagedList
import dev.yveskalume.newsappp.util.paging.PageSnapshot
import dev.yveskalume.newsappp.util.paging.collectAsStateWithLifecycle
import dev.yveskalume.newsappp.util.paging.rememberLazyPagedListState
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object HomeRoute : NavKey

fun NavBackStack<NavKey>.navigateToHome() {
    if (lastOrNull() !is HomeRoute) {
        clear()
        add(HomeRoute)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val sourcesUiState by viewModel.sourcesUiState.collectAsStateWithLifecycle()
    val refreshUiState by viewModel.refreshUiState.collectAsStateWithLifecycle()
    val articlesPageSnapshot by viewModel.articlePager.collectAsStateWithLifecycle()

    HomeScreen(
        articlesPageSnapshot = articlesPageSnapshot,
        refreshUiState = refreshUiState,
        viewModel = viewModel,
        sourcesUiState = sourcesUiState,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeScreen(
    articlesPageSnapshot: PageSnapshot<Article>,
    refreshUiState: RefreshUiState,
    viewModel: IHomeViewModel,
    sourcesUiState: SourcesUiState
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior)
        },
        bottomBar = {
            AnimatedVisibility(visible = articlesPageSnapshot.pageState.isLoading()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = refreshUiState is RefreshUiState.Refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .paddingAndConsumeWindowInsets(paddingValues)
        ) {
            HomeContent(
                sourcesUiState = sourcesUiState,
                articlesPageSnapshot = articlesPageSnapshot,
                onSourceClick = viewModel::selectSource,
                onRetry = viewModel::refresh,
                onLoadMore = viewModel::loadMore
            )
        }
    }
}


@Composable
private fun HomeContent(
    sourcesUiState: SourcesUiState,
    articlesPageSnapshot: PageSnapshot<Article>,
    onSourceClick: (SourceItem?) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {

    val state = rememberLazyPagedListState(articlesPageSnapshot)

    LazyPagedList(
        state = state,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        onLoadMore = onLoadMore
    ) {
        sourceItems(sourcesUiState = sourcesUiState, onSourceClick = onSourceClick)
        articleItems(articlesPageSnapshot.dataState, onRetry)
    }
}


private fun LazyListScope.sourceItems(
    sourcesUiState: SourcesUiState,
    onSourceClick: (SourceItem?) -> Unit
) {
    item("sources_section") {
        when (sourcesUiState) {
            SourcesUiState.Loading -> SourcesRowShimmer()
            is SourcesUiState.Success -> SourceContent(
                sourcesUiState = sourcesUiState,
                onSourceClick = onSourceClick
            )

            else -> {}
        }
    }
}


private fun LazyListScope.articleItems(
    articlesState: DataState<Article>,
    onRetry: () -> Unit
) {

    when (articlesState) {
        is DataState.Error<*> -> {
            item("articles_error") {
                ErrorContent(
                    message = articlesState.message,
                    onRetry = onRetry,
                    modifier = Modifier.height(400.dp)
                )
            }
        }

        DataState.Loading -> {
            items(5) {
                NewsCardShimmer()
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }

        is DataState.Success<Article> -> {
            if (articlesState.items.isEmpty()) {
                item("empty_articles") {
                    EmptyContent(
                        title = stringResource(R.string.no_news_available),
                        message = stringResource(R.string.there_are_no_articles_to_show),
                        modifier = Modifier.height(400.dp)
                    )
                }
            } else {
                items(
                    items = articlesState.items,
                    key = { it.url }
                ) { article ->
                    NewsCard(article = article)
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}


@Composable
private fun SourceContent(
    sourcesUiState: SourcesUiState.Success,
    onSourceClick: (SourceItem?) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        SourcesRow(
            sources = sourcesUiState.sources,
            selectedSource = sourcesUiState.selected,
            onSourceClick = onSourceClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomeScreenPreviewProvider::class) previewData: HomeScreenPreviewData
) {
    NewsApppTheme {
        HomeScreen(
            articlesPageSnapshot = previewData.articlesPageSnapshot,
            refreshUiState = previewData.refreshUiState,
            viewModel = object : IHomeViewModel {
                override fun selectSource(source: SourceItem?) {}
                override fun refresh() {}
                override fun loadMore() {}
            },
            sourcesUiState = previewData.sourcesUiState
        )
    }
}
