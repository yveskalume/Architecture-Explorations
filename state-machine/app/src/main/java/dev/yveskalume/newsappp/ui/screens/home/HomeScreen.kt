package dev.yveskalume.newsappp.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import dev.yveskalume.newsapp.domain.model.SourceItem
import dev.yveskalume.newsappp.R
import dev.yveskalume.newsappp.core.getScopedViewModel
import dev.yveskalume.newsappp.ui.components.EmptyContent
import dev.yveskalume.newsappp.ui.components.ErrorContent
import dev.yveskalume.newsappp.ui.components.NewsCard
import dev.yveskalume.newsappp.ui.components.NewsCardShimmer
import dev.yveskalume.newsappp.ui.components.SourcesRow
import dev.yveskalume.newsappp.ui.components.SourcesRowShimmer
import dev.yveskalume.newsappp.ui.preview.HomeScreenPreviewProvider
import dev.yveskalume.newsappp.ui.screens.home.components.TopAppBar
import dev.yveskalume.newsappp.ui.screens.home.di.HomeDiContainer
import dev.yveskalume.newsappp.ui.theme.NewsApppTheme
import dev.yveskalume.newsappp.util.ListPagingEffect
import dev.yveskalume.newsappp.util.paddingAndConsumeWindowInsets
import kotlinx.serialization.Serializable

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
    stateManager: HomeViewModel = getScopedViewModel(HomeDiContainer)
) {
    val uiState by stateManager.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = stateManager::onEvent
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior)
        },
        bottomBar = {
            AnimatedVisibility(visible = uiState.articleUiState.isLoadingMore) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.refreshUiState is RefreshUiState.Refreshing,
            onRefresh = { onEvent(HomeEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .paddingAndConsumeWindowInsets(paddingValues)
        ) {
            HomeContent(
                sourcesUiState = uiState.sourcesUiState,
                articleUiState = uiState.articleUiState,
                onSourceClick = { onEvent(HomeEvent.SelectSource(it)) },
                onRetry = { onEvent(HomeEvent.Refresh) },
                onLoadMore = { onEvent(HomeEvent.LoadMore) }
            )
        }
    }
}


@Composable
private fun HomeContent(
    sourcesUiState: SourcesUiState,
    articleUiState: ArticleUiState,
    onSourceClick: (SourceItem?) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    ListPagingEffect(
        listState = listState,
        canLoadMore = articleUiState.canLoadMore,
        onLoadMore = onLoadMore
    )

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        sourceItems(sourcesUiState = sourcesUiState, onSourceClick = onSourceClick)
        articleItems(articleUiState, onRetry)
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
    articleUiState: ArticleUiState,
    onRetry: () -> Unit
) {
    when (articleUiState) {
        is ArticleUiState.Error -> {
            item("news_error") {
                ErrorContent(
                    message = articleUiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.height(400.dp)
                )
            }
        }

        ArticleUiState.Loading -> {
            items(5) {
                NewsCardShimmer()
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }

        is ArticleUiState.Success -> {
            if (articleUiState.articles.isEmpty()) {
                item("empty_news") {
                    EmptyContent(
                        title = stringResource(R.string.no_news_available),
                        message = stringResource(R.string.there_are_no_articles_to_show),
                        modifier = Modifier.height(400.dp)
                    )
                }
            } else {
                items(
                    items = articleUiState.articles,
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
    @PreviewParameter(HomeScreenPreviewProvider::class) uiState: HomeUiState
) {
    NewsApppTheme {
        HomeScreen(
            uiState = uiState,
            onEvent = {}
        )
    }
}


