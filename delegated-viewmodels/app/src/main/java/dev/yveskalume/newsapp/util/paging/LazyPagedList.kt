package dev.yveskalume.newsapp.util.paging

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun LazyPagedList(
    state: LazyPagedListState,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: LazyListScope.() -> Unit,
) {
    val listState = state.lazyListState
    val pagerState = state.pagerState.pageState

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        content = content,
    )

    LaunchedEffect(listState, pagerState) {
        snapshotFlow {
            listState.canScrollForward to listState.layoutInfo.totalItemsCount
        }.distinctUntilChanged().collect { (canScrollForward, totalItems) ->
            val shouldLoadMore = totalItems > 0 && !canScrollForward
            val canLoadMore = pagerState is PageState.Idle
            if (shouldLoadMore && canLoadMore) {
                onLoadMore()
            }
        }
    }
}

@Composable
fun rememberLazyPagedListState(
    pagerState: PageSnapshot<*>,
    listState: LazyListState = rememberLazyListState(),
): LazyPagedListState {
    return remember(listState, pagerState) {
        LazyPagedListState(listState, pagerState)
    }
}

data class LazyPagedListState(
    val lazyListState: LazyListState,
    val pagerState: PageSnapshot<*>,
)
