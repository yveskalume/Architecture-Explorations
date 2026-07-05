package dev.yveskalume.newsapp.ui.logic

import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.util.paging.DataState
import dev.yveskalume.newsapp.util.paging.PageNumber
import dev.yveskalume.newsapp.util.paging.PageSnapshot
import dev.yveskalume.newsapp.util.paging.PageState

private const val PAGE_SIZE = 20

class GetArticleLogic(
    private val articleRepository: ArticleRepository,
) {

    suspend fun load(
        snapshot: PageSnapshot<Article>,
        sourceId: String? = null,
        query: String? = null,
        page: PageNumber,
    ): Result<PageSnapshot<Article>> {
        val reset = page.value == 1

        return articleRepository.getTopHeadlines(
            sources = sourceId,
            query = query,
            pageSize = PAGE_SIZE,
            page = page.value,
        ).map { articles ->
            val oldArticles = (snapshot.dataState as? DataState.Success)?.items.orEmpty()
            val updatedArticles = if (reset) articles else oldArticles + articles

            snapshot.copy(
                currentPage = snapshot.currentPage.takeIf { articles.isEmpty() } ?: page,
                pageState = if (articles.isEmpty()) PageState.EndReached else PageState.Idle,
                dataState = DataState.Success(updatedArticles),
            )
        }
    }
}
