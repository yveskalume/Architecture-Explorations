package dev.yveskalume.newsappp.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.yveskalume.newsappp.R
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.ui.components.EmptyContent
import dev.yveskalume.newsappp.ui.components.NewsCardCompact
import dev.yveskalume.newsappp.ui.components.NewsCardCompactShimmer
import dev.yveskalume.newsappp.ui.components.SearchTextField
import dev.yveskalume.newsappp.ui.preview.SearchScreenPreviewData
import dev.yveskalume.newsappp.ui.preview.SearchScreenPreviewProvider
import dev.yveskalume.newsappp.ui.theme.NewsApppTheme
import dev.yveskalume.newsappp.util.paging.DataState
import dev.yveskalume.newsappp.util.paging.LazyPagedList
import dev.yveskalume.newsappp.util.paging.PageState
import dev.yveskalume.newsappp.util.paging.PageSnapshot
import dev.yveskalume.newsappp.util.paging.collectAsStateWithLifecycle
import dev.yveskalume.newsappp.util.paging.rememberLazyPagedListState
import dev.yveskalume.newsappp.util.paddingAndConsumeWindowInsets
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object SearchRoute : NavKey

fun NavBackStack<NavKey>.navigateToSearch() {
    if (lastOrNull() !is SearchRoute) {
        add(SearchRoute)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenRoute(
    viewModel: SearchViewModel = koinViewModel()
) {
    val queryText by viewModel.queryUiState.collectAsStateWithLifecycle()
    val articlePageSnapshot by viewModel.articlePager.collectAsStateWithLifecycle()
    SearchScreen(
        queryText = queryText,
        articlePageSnapshot = articlePageSnapshot,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    queryText: String,
    articlePageSnapshot: PageSnapshot<Article>,
    viewModel: ISearchViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = articlePageSnapshot.pageState is PageState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paddingAndConsumeWindowInsets(paddingValues)
        ) {
            SearchTextField(
                queryText = queryText,
                onQueryChange = viewModel::onQueryChange,
                onClearSearch = viewModel::clearSearch
            )

            SearchContent(
                queryText = queryText,
                articlePageSnapshot = articlePageSnapshot,
                onLoadMore = viewModel::loadMore
            )
        }
    }
}

@Composable
private fun SearchContent(
    queryText: String,
    articlePageSnapshot: PageSnapshot<Article>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val pagedListState = rememberLazyPagedListState(
        pagerState = articlePageSnapshot,
        listState = listState
    )

    val normalizedQuery = queryText.trim()
    if (normalizedQuery.isBlank()) {
        EmptyContent(
            title = stringResource(R.string.search_for_news),
            message = stringResource(R.string.enter_keywords_to_search),
            modifier = modifier
        )
        return
    }

    when (val dataState = articlePageSnapshot.dataState) {
        DataState.Loading -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(5) {
                    NewsCardCompactShimmer()
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        is DataState.Error -> {
            EmptyContent(
                title = stringResource(R.string.something_went_wrong),
                message = dataState.message,
                modifier = modifier
            )
        }

        is DataState.Success -> {
            if (dataState.items.isEmpty()) {
                EmptyContent(
                    title = stringResource(R.string.no_results_found),
                    message = stringResource(R.string.try_different_keywords_or_check_your_spelling),
                    modifier = modifier
                )
            } else {
                LazyPagedList(
                    state = pagedListState,
                    onLoadMore = onLoadMore,
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = dataState.items,
                        key = { it.url }
                    ) { article ->
                        NewsCardCompact(article = article)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchScreenPreviewProvider::class) previewData: SearchScreenPreviewData
) {
    NewsApppTheme {
        SearchScreen(
            queryText = previewData.queryText,
            articlePageSnapshot = previewData.articlePageSnapshot,
            viewModel = object : ISearchViewModel {
                override fun onQueryChange(query: String) {}
                override fun clearSearch() {}
                override fun loadMore() {}
            }
        )
    }
}
