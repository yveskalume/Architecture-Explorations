package dev.yveskalume.newsappp.ui.screens.home

import dev.yveskalume.newsappp.fake.FakeArticleRepositoryConfigurable
import dev.yveskalume.newsappp.fake.FakeArticleRepositoryFailure
import dev.yveskalume.newsappp.fake.FakeArticleRepositorySuccess
import dev.yveskalume.newsappp.fake.FakeSourcesRepositoryFailure
import dev.yveskalume.newsappp.fake.FakeSourcesRepositorySuccess
import dev.yveskalume.newsappp.util.MainDispatcherRule
import dev.yveskalume.newsappp.util.paging.DataState
import dev.yveskalume.newsappp.util.paging.PageNumber
import dev.yveskalume.newsappp.util.paging.PageState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // region Initial State Tests

    @Test
    fun `initial articlePager snapshot should be Loading`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // Then
        val snapshot = viewModel.articlePager.snapshot.value
        assertEquals(DataState.Loading, snapshot.dataState)
        assertEquals(PageState.Idle, snapshot.pageState)
        assertNull(snapshot.currentPage)
    }

    @Test
    fun `initial sourcesUiState should be Loading`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // Then
        assertEquals(SourcesUiState.Loading, viewModel.sourcesUiState.value)
    }

    @Test
    fun `initial refreshUiState should be Idle`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // Then
        assertEquals(RefreshUiState.Idle, viewModel.refreshUiState.value)
    }

    // endregion

    // region Articles Pager Tests

    @Test
    fun `articlePager snapshot should emit Success with articles when fetch succeeds`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // When
        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        // Then
        val snapshot = viewModel.articlePager.snapshot.value
        assertEquals(PageNumber(1), snapshot.currentPage)
        assertEquals(PageState.Idle, snapshot.pageState)
        assertTrue(snapshot.dataState is DataState.Success)
        val successState = snapshot.dataState as DataState.Success
        assertEquals(FakeArticleRepositorySuccess.sampleArticles, successState.items)

        collectJob.cancel()
    }

    @Test
    fun `articlePager snapshot should emit Error when initial fetch fails`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositoryFailure("Custom network error"),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // When
        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        // Then
        val snapshot = viewModel.articlePager.snapshot.value
        assertTrue(snapshot.dataState is DataState.Error)
        val error = snapshot.dataState as DataState.Error
        assertEquals("Custom network error", error.message)
        assertEquals(PageState.Idle, snapshot.pageState)
        assertNull(snapshot.currentPage)

        collectJob.cancel()
    }

    // endregion

    // region Sources Success Tests

    @Test
    fun `sourcesUiState should emit Success with sources when fetch succeeds`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // When
        val collectJob = launch {
            viewModel.sourcesUiState.collect()
        }
        advanceUntilIdle()

        // Then
        val sourcesState = viewModel.sourcesUiState.value
        assertTrue(sourcesState is SourcesUiState.Success)
        val successState = sourcesState as SourcesUiState.Success
        assertEquals(FakeSourcesRepositorySuccess.sampleSources, successState.sources)

        collectJob.cancel()
    }

    @Test
    fun `sourcesUiState Success should have null selected initially`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        // When
        val collectJob = launch {
            viewModel.sourcesUiState.collect()
        }
        advanceUntilIdle()

        // Then
        val sourcesState = viewModel.sourcesUiState.value
        assertTrue(sourcesState is SourcesUiState.Success)
        val successState = sourcesState as SourcesUiState.Success
        assertNull(successState.selected)

        collectJob.cancel()
    }

    // endregion

    // region Sources Failure Tests

    @Test
    fun `sourcesUiState should emit Error when fetch fails`() = runTest {
        // Given
        val errorMessage = "Sources fetch error"
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositoryFailure(errorMessage)
        )

        // When
        val collectJob = launch {
            viewModel.sourcesUiState.collect()
        }
        advanceUntilIdle()

        // Then
        val sourcesState = viewModel.sourcesUiState.value
        assertTrue(sourcesState is SourcesUiState.Error)
        val errorState = sourcesState as SourcesUiState.Error
        assertEquals(errorMessage, errorState.message)

        collectJob.cancel()
    }

    // endregion

    // region Select Source Tests

    @Test
    fun `selectSource should update selected source in sourcesUiState`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.sourcesUiState.collect()
        }
        advanceUntilIdle()

        // When
        val sourceToSelect = FakeSourcesRepositorySuccess.sampleSources.first()
        viewModel.selectSource(sourceToSelect)
        advanceUntilIdle()

        // Then
        val sourcesState = viewModel.sourcesUiState.value
        assertTrue(sourcesState is SourcesUiState.Success)
        val successState = sourcesState as SourcesUiState.Success
        assertEquals(sourceToSelect, successState.selected)

        collectJob.cancel()
    }

    @Test
    fun `selectSource with same source should deselect it`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.sourcesUiState.collect()
        }
        advanceUntilIdle()

        // Select a source first
        val sourceToSelect = FakeSourcesRepositorySuccess.sampleSources.first()
        viewModel.selectSource(sourceToSelect)
        advanceUntilIdle()

        // When - select same source again
        viewModel.selectSource(sourceToSelect)
        advanceUntilIdle()

        // Then - should be deselected (null)
        val sourcesState = viewModel.sourcesUiState.value
        val successState = sourcesState as SourcesUiState.Success
        assertNull(successState.selected)

        collectJob.cancel()
    }

    @Test
    fun `selectSource with null should clear selection`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.sourcesUiState.collect()
        }
        advanceUntilIdle()

        // Select a source first
        viewModel.selectSource(FakeSourcesRepositorySuccess.sampleSources.first())
        advanceUntilIdle()

        // When
        viewModel.selectSource(null)
        advanceUntilIdle()

        // Then
        val sourcesState = viewModel.sourcesUiState.value as SourcesUiState.Success
        assertNull(sourcesState.selected)

        collectJob.cancel()
    }

    // endregion

    @Test
    fun `selectSource should retry and reload articles with selected source id`() = runTest {
        // Given
        val repository = RecordingArticleRepository()
        val viewModel = HomeViewModel(
            articleRepository = repository,
            sourcesRepository = FakeSourcesRepositorySuccess()
        )
        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()
        val sourceToSelect = FakeSourcesRepositorySuccess.sampleSources.first()

        // When
        viewModel.selectSource(sourceToSelect)
        advanceUntilIdle()

        // Then
        assertTrue(repository.requestedSources.isNotEmpty())
        assertEquals(sourceToSelect.id, repository.requestedSources.last())
        val snapshot = viewModel.articlePager.snapshot.value
        assertTrue(snapshot.dataState is DataState.Success)
        collectJob.cancel()
    }

    // endregion

    // region Refresh Tests

    @Test
    fun `refresh should set refreshUiState to Refreshing`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        // When
        viewModel.refresh()

        // Then - check immediate state after calling refresh
        assertEquals(RefreshUiState.Refreshing, viewModel.refreshUiState.value)

        collectJob.cancel()
    }

    // endregion

    // region Load More (Pagination) Tests

    @Test
    fun `loadMore should append articles when successful`() = runTest {
        // Given
        val fakeRepository = FakeArticleRepositoryConfigurable()
        val viewModel = HomeViewModel(
            articleRepository = fakeRepository,
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        val initialSnapshot = viewModel.articlePager.snapshot.value
        val initialArticles = (initialSnapshot.dataState as DataState.Success).items

        // When
        viewModel.loadMore()
        advanceUntilIdle()

        // Then
        val finalSnapshot = viewModel.articlePager.snapshot.value
        assertTrue(finalSnapshot.dataState is DataState.Success)
        val successState = finalSnapshot.dataState as DataState.Success

        // Should have initial articles + page 2 articles
        val expectedCount = initialArticles.size + fakeRepository.page2Articles.size
        assertEquals(expectedCount, successState.items.size)
        assertEquals(PageNumber(2), finalSnapshot.currentPage)
        assertEquals(PageState.Idle, finalSnapshot.pageState)

        collectJob.cancel()
    }

    @Test
    fun `loadMore should set paging state to EndReached when no more articles`() = runTest {
        // Given
        val fakeRepository = FakeArticleRepositoryConfigurable().apply {
            page2Articles = emptyList() // Simulate no more articles
        }
        val viewModel = HomeViewModel(
            articleRepository = fakeRepository,
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        // When
        viewModel.loadMore()
        advanceUntilIdle()

        // Then
        val finalSnapshot = viewModel.articlePager.snapshot.value
        assertEquals(PageState.EndReached, finalSnapshot.pageState)
        assertEquals(PageNumber(1), finalSnapshot.currentPage)
        assertTrue(finalSnapshot.dataState is DataState.Success)

        collectJob.cancel()
    }

    @Test
    fun `loadMore should not fetch when paging state is not Idle`() = runTest {
        // Given
        val fakeRepository = FakeArticleRepositoryConfigurable().apply {
            page2Articles = emptyList()
        }
        val viewModel = HomeViewModel(
            articleRepository = fakeRepository,
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()

        // Load more to reach EndReached state
        viewModel.loadMore()
        advanceUntilIdle()

        // When - try to load more again
        val stateBeforeSecondLoad = viewModel.articlePager.snapshot.value
        viewModel.loadMore()
        advanceUntilIdle()

        // Then - state should remain unchanged (EndReached)
        val stateAfterSecondLoad = viewModel.articlePager.snapshot.value
        assertEquals(stateBeforeSecondLoad, stateAfterSecondLoad)

        collectJob.cancel()
    }

    @Test
    fun `loadMore failure should keep previous data and return Idle page state`() = runTest {
        // Given
        val fakeRepository = FakeArticleRepositoryConfigurable()
        val viewModel = HomeViewModel(
            articleRepository = fakeRepository,
            sourcesRepository = FakeSourcesRepositorySuccess()
        )

        val collectJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()
        val before = viewModel.articlePager.snapshot.value
        val beforeItems = (before.dataState as DataState.Success).items

        // Configure repository to fail for next call
        fakeRepository.shouldFail = true
        fakeRepository.errorMessage = "Failed to load more"

        // When
        viewModel.loadMore()
        advanceUntilIdle()

        // Then
        val after = viewModel.articlePager.snapshot.value
        assertEquals(PageState.Idle, after.pageState)
        assertTrue(after.dataState is DataState.Success)
        val afterItems = (after.dataState as DataState.Success).items
        assertEquals(beforeItems, afterItems)
        assertEquals(before.currentPage, after.currentPage)

        collectJob.cancel()
    }

    // endregion

    @Test
    fun `refresh should restart sources stream and keep sources success state`() = runTest {
        // Given
        val viewModel = HomeViewModel(
            articleRepository = FakeArticleRepositorySuccess(),
            sourcesRepository = FakeSourcesRepositorySuccess()
        )
        val sourcesJob = launch {
            viewModel.sourcesUiState.collect()
        }
        val pagerJob = launch {
            viewModel.articlePager.snapshot.collect()
        }
        advanceUntilIdle()
        assertTrue(viewModel.sourcesUiState.value is SourcesUiState.Success)

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        val state = viewModel.sourcesUiState.value
        assertTrue(state is SourcesUiState.Success)
        val success = state as SourcesUiState.Success
        assertNotNull(success.sources)
        assertFalse(success.sources.isEmpty())

        sourcesJob.cancel()
        pagerJob.cancel()
    }
}

private class RecordingArticleRepository : dev.yveskalume.newsapp.data.repository.ArticleRepository {
    val requestedSources = mutableListOf<String?>()

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): Result<List<dev.yveskalume.newsapp.domain.model.Article>> {
        requestedSources += sources
        return Result.success(FakeArticleRepositorySuccess.sampleArticles)
    }
}
