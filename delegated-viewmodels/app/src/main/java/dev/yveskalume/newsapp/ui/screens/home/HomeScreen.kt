package dev.yveskalume.newsapp.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.ui.components.EmptyContent
import dev.yveskalume.newsapp.ui.components.ErrorContent
import dev.yveskalume.newsapp.ui.components.NewsCard
import dev.yveskalume.newsapp.ui.components.NewsCardShimmer
import dev.yveskalume.newsapp.ui.components.SourcesRow
import dev.yveskalume.newsapp.ui.components.SourcesRowShimmer
import dev.yveskalume.newsapp.ui.screens.home.controllers.ArticlesController
import dev.yveskalume.newsapp.ui.screens.home.controllers.RefreshController
import dev.yveskalume.newsapp.ui.screens.home.controllers.SourcesController
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.LazyPagedList
import dev.yveskalume.newsapp.util.paging.PageSnapshot
import dev.yveskalume.newsapp.util.paging.rememberLazyPagedListState
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

@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        controllers = viewModel.controllers,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    controllers: HomeControllers,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Discover") },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            if (state.articlePageSnapshot.pageState.isLoading()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.refreshUiState is RefreshUiState.Refreshing,
            onRefresh = controllers::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            HomeContent(
                state = state,
                controllers = controllers,
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    controllers: HomeControllers,
    modifier: Modifier = Modifier,
) {
    state.error?.let { message ->
        ErrorContent(
            message = message,
            onRetry = controllers::refresh,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val listState = rememberLazyPagedListState(state.articlePageSnapshot)

    LazyPagedList(
        state = listState,
        onLoadMore = controllers::loadMore,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item("sources") {
            SourcesSection(
                state = state.sourcesUiState,
                controller = controllers,
            )
        }

        articleItems(
            articlePageSnapshot = state.articlePageSnapshot,
            controller = controllers,
        )
    }
}

@Composable
private fun SourcesSection(
    state: SourcesUiState,
    controller: SourcesController,
    modifier: Modifier = Modifier,
) {
    if (state is SourcesUiState.Error) return

    Column(modifier = modifier) {
        when (state) {
            SourcesUiState.Loading -> SourcesRowShimmer()
            is SourcesUiState.Success -> SourcesRow(
                sources = state.sources,
                selectedSource = state.selected,
                onSourceClick = controller::selectSource,
            )
            is SourcesUiState.Error -> Unit
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

private fun LazyListScope.articleItems(
    articlePageSnapshot: PageSnapshot<Article>,
    controller: ArticlesController,
) {
    when (val dataState = articlePageSnapshot.dataState) {
        DataState.Loading -> {
            items(5) {
                NewsCardShimmer()
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
        is DataState.Success -> {
            if (dataState.items.isEmpty()) {
                item("empty_articles") {
                    EmptyContent(
                        title = "No news available",
                        message = "There are no articles to show.",
                        modifier = Modifier.height(400.dp),
                    )
                }
            } else {
                items(
                    items = dataState.items,
                    key = { it.url },
                ) { article ->
                    NewsCard(article = article)
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
        is DataState.Error -> {
            item("articles_error") {
                ErrorContent(
                    message = dataState.message,
                    onRetry = controller::retry,
                    modifier = Modifier.height(400.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            state = HomeUiState.initial(),
            controllers = HomeControllers(
                sourcesController = object : SourcesController {
                    override fun selectSource(source: dev.yveskalume.newsapp.domain.model.SourceItem?) = Unit
                },
                articlesController = object : ArticlesController {
                    override fun retry() = Unit
                    override fun loadMore() = Unit
                },
                refreshController = object : RefreshController {
                    override fun refresh() = Unit
                },
            ),
        )
    }
}
