package dev.yveskalume.newsappp.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.ui.screens.home.RefreshUiState
import dev.yveskalume.newsappp.ui.screens.home.SourcesUiState
import dev.yveskalume.newsappp.util.paging.DataState
import dev.yveskalume.newsappp.util.paging.PageNumber
import dev.yveskalume.newsappp.util.paging.PageSnapshot
import dev.yveskalume.newsappp.util.paging.PageState

data class HomeScreenPreviewData(
    val sourcesUiState: SourcesUiState,
    val refreshUiState: RefreshUiState,
    val articlesPageSnapshot: PageSnapshot<Article>
)

class HomeScreenPreviewProvider : PreviewParameterProvider<HomeScreenPreviewData> {

    override val values: Sequence<HomeScreenPreviewData> = sequenceOf(
        // Success state with articles
        HomeScreenPreviewData(
            sourcesUiState = SourcesUiState.Success(
                sources = PreviewSampleData.sampleSources,
                selected = null
            ),
            refreshUiState = RefreshUiState.Idle,
            articlesPageSnapshot = PageSnapshot(
                currentPage = PageNumber(1),
                pageState = PageState.Idle,
                dataState = DataState.Success(PreviewSampleData.sampleArticles)
            )
        ),
        // Success state with selected source
        HomeScreenPreviewData(
            sourcesUiState = SourcesUiState.Success(
                sources = PreviewSampleData.sampleSources,
                selected = PreviewSampleData.sampleSources.first()
            ),
            refreshUiState = RefreshUiState.Idle,
            articlesPageSnapshot = PageSnapshot(
                currentPage = PageNumber(1),
                pageState = PageState.Idle,
                dataState = DataState.Success(PreviewSampleData.sampleArticles.take(2))
            )
        ),
        // Loading state
        HomeScreenPreviewData(
            sourcesUiState = SourcesUiState.Loading,
            refreshUiState = RefreshUiState.Idle,
            articlesPageSnapshot = PageSnapshot(
                dataState = DataState.Loading
            )
        ),
        // Error state
        HomeScreenPreviewData(
            sourcesUiState = SourcesUiState.Success(
                sources = PreviewSampleData.sampleSources,
                selected = null
            ),
            refreshUiState = RefreshUiState.Idle,
            articlesPageSnapshot = PageSnapshot(
                dataState = DataState.Error(
                    message = "Failed to load news. Please check your internet connection."
                )
            )
        ),
        // Empty state
        HomeScreenPreviewData(
            sourcesUiState = SourcesUiState.Success(
                sources = PreviewSampleData.sampleSources,
                selected = null
            ),
            refreshUiState = RefreshUiState.Refreshing,
            articlesPageSnapshot = PageSnapshot(
                currentPage = PageNumber(1),
                pageState = PageState.EndReached,
                dataState = DataState.Success(emptyList())
            )
        )
    )
}
