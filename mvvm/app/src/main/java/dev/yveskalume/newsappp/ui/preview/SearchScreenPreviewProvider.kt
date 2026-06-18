package dev.yveskalume.newsappp.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsappp.util.paging.DataState
import dev.yveskalume.newsappp.util.paging.PageNumber
import dev.yveskalume.newsappp.util.paging.PageSnapshot
import dev.yveskalume.newsappp.util.paging.PageState

data class SearchScreenPreviewData(
    val queryText: String,
    val articlePageSnapshot: PageSnapshot<Article>
)

class SearchScreenPreviewProvider : PreviewParameterProvider<SearchScreenPreviewData> {

    override val values: Sequence<SearchScreenPreviewData> = sequenceOf(
        SearchScreenPreviewData(
            queryText = "",
            articlePageSnapshot = PageSnapshot(
                dataState = DataState.Success(emptyList())
            )
        ),
        SearchScreenPreviewData(
            queryText = "kotlin",
            articlePageSnapshot = PageSnapshot(
                dataState = DataState.Loading
            )
        ),
        SearchScreenPreviewData(
            queryText = "kotlin",
            articlePageSnapshot = PageSnapshot(
                currentPage = PageNumber(1),
                pageState = PageState.Idle,
                dataState = DataState.Success(PreviewSampleData.sampleArticles)
            )
        ),
        SearchScreenPreviewData(
            queryText = "kotlin",
            articlePageSnapshot = PageSnapshot(
                currentPage = PageNumber(1),
                pageState = PageState.Loading,
                dataState = DataState.Success(PreviewSampleData.sampleArticles)
            )
        ),
        SearchScreenPreviewData(
            queryText = "missing",
            articlePageSnapshot = PageSnapshot(
                currentPage = PageNumber(1),
                pageState = PageState.EndReached,
                dataState = DataState.Success(emptyList())
            )
        ),
        SearchScreenPreviewData(
            queryText = "kotlin",
            articlePageSnapshot = PageSnapshot(
                dataState = DataState.Error("Search failed. Please retry.")
            )
        )
    )
}
