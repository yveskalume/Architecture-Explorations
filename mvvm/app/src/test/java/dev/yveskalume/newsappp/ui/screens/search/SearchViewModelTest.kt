package dev.yveskalume.newsappp.ui.screens.search

import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.fake.FakeArticleRepositoryConfigurable
import dev.yveskalume.newsappp.fake.FakeArticleRepositoryFailure
import dev.yveskalume.newsappp.fake.FakeArticleRepositorySuccess
import dev.yveskalume.newsappp.util.MainDispatcherRule
import dev.yveskalume.newsappp.util.paging.DataState
import dev.yveskalume.newsappp.util.paging.PageNumber
import dev.yveskalume.newsappp.util.paging.PageState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial query should be empty and pager snapshot should be default loading`() = runTest {
        val viewModel = SearchViewModel(
            articleRepository = FakeArticleRepositorySuccess()
        )

        assertEquals("", viewModel.queryUiState.value)
        val snapshot = viewModel.articlePager.snapshot.value
        assertEquals(DataState.Loading, snapshot.dataState)
        assertEquals(PageState.Idle, snapshot.pageState)
        assertNull(snapshot.currentPage)
    }

    @Test
    fun `collecting pager with blank query should produce empty success and EndReached`() = runTest {
        val viewModel = SearchViewModel(
            articleRepository = FakeArticleRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        val snapshot = viewModel.articlePager.snapshot.value
        assertTrue(snapshot.dataState is DataState.Success)
        val success = snapshot.dataState as DataState.Success
        assertTrue(success.items.isEmpty())
        assertEquals(PageState.EndReached, snapshot.pageState)
        assertEquals(PageNumber(1), snapshot.currentPage)

        collectJob.cancel()
    }

    @Test
    fun `onQueryChange should update query and load articles`() = runTest {
        val viewModel = SearchViewModel(
            articleRepository = FakeArticleRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        viewModel.onQueryChange("kotlin")
        advanceUntilIdle()

        assertEquals("kotlin", viewModel.queryUiState.value)
        val snapshot = viewModel.articlePager.snapshot.value
        assertEquals(PageState.Idle, snapshot.pageState)
        assertEquals(PageNumber(1), snapshot.currentPage)
        assertTrue(snapshot.dataState is DataState.Success)
        val success = snapshot.dataState as DataState.Success
        assertEquals(FakeArticleRepositorySuccess.sampleArticles, success.items)

        collectJob.cancel()
    }

    @Test
    fun `onQueryChange should pass trimmed query to repository`() = runTest {
        val repository = RecordingArticleRepository()
        val viewModel = SearchViewModel(
            articleRepository = repository
        )

        viewModel.onQueryChange("   kotlin compose   ")
        advanceUntilIdle()

        assertEquals("   kotlin compose   ", viewModel.queryUiState.value)
        assertEquals("kotlin compose", repository.requestedQueries.last())
    }

    @Test
    fun `search failure after blank baseline should keep empty success state`() = runTest {
        val viewModel = SearchViewModel(
            articleRepository = FakeArticleRepositoryFailure("Search failed")
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        viewModel.onQueryChange("android")
        advanceUntilIdle()

        val snapshot = viewModel.articlePager.snapshot.value
        assertTrue(snapshot.dataState is DataState.Success)
        val success = snapshot.dataState as DataState.Success
        assertTrue(success.items.isEmpty())
        assertEquals(PageState.Idle, snapshot.pageState)
        assertEquals(PageNumber(1), snapshot.currentPage)

        collectJob.cancel()
    }

    @Test
    fun `clearSearch should reset query and show empty success`() = runTest {
        val viewModel = SearchViewModel(
            articleRepository = FakeArticleRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        viewModel.onQueryChange("kotlin")
        advanceUntilIdle()

        viewModel.clearSearch()
        advanceUntilIdle()

        assertEquals("", viewModel.queryUiState.value)
        val snapshot = viewModel.articlePager.snapshot.value
        assertTrue(snapshot.dataState is DataState.Success)
        val success = snapshot.dataState as DataState.Success
        assertTrue(success.items.isEmpty())
        assertEquals(PageState.EndReached, snapshot.pageState)

        collectJob.cancel()
    }

    @Test
    fun `loadMore should append articles when successful`() = runTest {
        val fakeRepository = FakeArticleRepositoryConfigurable()
        val viewModel = SearchViewModel(
            articleRepository = fakeRepository
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        viewModel.onQueryChange("test")
        advanceUntilIdle()
        val initialSnapshot = viewModel.articlePager.snapshot.value
        val initialItems = (initialSnapshot.dataState as DataState.Success).items

        viewModel.loadMore()
        advanceUntilIdle()

        val finalSnapshot = viewModel.articlePager.snapshot.value
        assertTrue(finalSnapshot.dataState is DataState.Success)
        val success = finalSnapshot.dataState as DataState.Success
        val expectedCount = initialItems.size + fakeRepository.page2Articles.size
        assertEquals(expectedCount, success.items.size)
        assertEquals(PageNumber(2), finalSnapshot.currentPage)
        assertEquals(PageState.Idle, finalSnapshot.pageState)

        collectJob.cancel()
    }

    @Test
    fun `loadMore should set EndReached when next page is empty`() = runTest {
        val fakeRepository = FakeArticleRepositoryConfigurable().apply {
            page2Articles = emptyList()
        }
        val viewModel = SearchViewModel(
            articleRepository = fakeRepository
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        viewModel.onQueryChange("test")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val finalSnapshot = viewModel.articlePager.snapshot.value
        assertEquals(PageState.EndReached, finalSnapshot.pageState)
        assertEquals(PageNumber(1), finalSnapshot.currentPage)

        collectJob.cancel()
    }

    @Test
    fun `loadMore failure should keep previous data and return Idle page state`() = runTest {
        val fakeRepository = FakeArticleRepositoryConfigurable()
        val viewModel = SearchViewModel(
            articleRepository = fakeRepository
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        viewModel.onQueryChange("test")
        advanceUntilIdle()

        val before = viewModel.articlePager.snapshot.value
        val beforeItems = (before.dataState as DataState.Success).items

        fakeRepository.shouldFail = true
        fakeRepository.errorMessage = "Failed to load more results"

        viewModel.loadMore()
        advanceUntilIdle()

        val after = viewModel.articlePager.snapshot.value
        assertEquals(PageState.Idle, after.pageState)
        assertTrue(after.dataState is DataState.Success)
        val afterItems = (after.dataState as DataState.Success).items
        assertEquals(beforeItems, afterItems)
        assertEquals(before.currentPage, after.currentPage)

        collectJob.cancel()
    }

    @Test
    fun `loadMore should do nothing when query is blank`() = runTest {
        val viewModel = SearchViewModel(
            articleRepository = FakeArticleRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        val before = viewModel.articlePager.snapshot.value
        viewModel.loadMore()
        advanceUntilIdle()
        val after = viewModel.articlePager.snapshot.value

        assertEquals(before, after)

        collectJob.cancel()
    }
}

private class RecordingArticleRepository : ArticleRepository {
    val requestedQueries = mutableListOf<String?>()

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> {
        requestedQueries += query
        val items = when (page) {
            1 -> FakeArticleRepositorySuccess.sampleArticles
            2 -> FakeArticleRepositorySuccess.page2Articles
            else -> emptyList()
        }
        return Result.success(items)
    }
}
