package dev.yveskalume.newsapp.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.yveskalume.newsapp.R
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.ui.components.EmptyContent
import dev.yveskalume.newsapp.ui.components.NewsCardCompact
import dev.yveskalume.newsapp.ui.components.NewsCardCompactShimmer
import dev.yveskalume.newsapp.ui.components.SearchTextField
import dev.yveskalume.newsapp.ui.screens.search.controllers.SearchController
import dev.yveskalume.newsapp.ui.theme.NewsAppTheme
import dev.yveskalume.newsapp.util.paddingAndConsumeWindowInsets
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.LazyPagedList
import dev.yveskalume.newsapp.util.paging.PageSnapshot
import dev.yveskalume.newsapp.util.paging.rememberLazyPagedListState
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SearchRoute : NavKey

fun NavBackStack<NavKey>.navigateToSearch() {
    if (lastOrNull() !is SearchRoute) {
        add(SearchRoute)
    }
}

@Composable
fun SearchScreenRoute(
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        state = state,
        controllers = viewModel.controllers,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    state: SearchUiState,
    controllers: SearchControllers,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = state.isLoadingMore) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paddingAndConsumeWindowInsets(paddingValues),
        ) {
            SearchTextField(
                queryText = state.query,
                onQueryChange = controllers::onQueryChanged,
                onClearSearch = controllers::clearSearch,
            )

            SearchContent(
                query = state.query,
                articlePageSnapshot = state.articlePageSnapshot,
                onLoadMore = controllers::loadMore,
            )
        }
    }
}

@Composable
private fun SearchContent(
    query: String,
    articlePageSnapshot: PageSnapshot<Article>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) {
        EmptyContent(
            title = stringResource(R.string.search_for_news),
            message = stringResource(R.string.enter_keywords_to_search),
            modifier = modifier,
        )
        return
    }

    when (val dataState = articlePageSnapshot.dataState) {
        DataState.Loading -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                loadingItems()
            }
        }

        is DataState.Error -> {
            EmptyContent(
                title = stringResource(R.string.something_went_wrong),
                message = dataState.message,
                modifier = modifier,
            )
        }

        is DataState.Success -> {
            if (dataState.items.isEmpty()) {
                EmptyContent(
                    title = stringResource(R.string.no_results_found),
                    message = stringResource(R.string.try_different_keywords_or_check_your_spelling),
                    modifier = modifier,
                )
            } else {
                val pagedListState = rememberLazyPagedListState(articlePageSnapshot)
                LazyPagedList(
                    state = pagedListState,
                    onLoadMore = onLoadMore,
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    articleItems(dataState.items)
                }
            }
        }
    }
}

private fun LazyListScope.loadingItems() {
    items(5) {
        NewsCardCompactShimmer()
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

private fun LazyListScope.articleItems(articles: List<Article>) {
    items(
        items = articles,
        key = { it.url },
    ) { article ->
        NewsCardCompact(article = article)
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    NewsAppTheme {
        SearchScreen(
            state = SearchUiState.initial(),
            controllers = SearchControllers(
                searchController = object : SearchController {
                    override fun onQueryChanged(query: String) = Unit
                    override fun clearSearch() = Unit
                    override fun loadMore() = Unit
                },
            ),
        )
    }
}
