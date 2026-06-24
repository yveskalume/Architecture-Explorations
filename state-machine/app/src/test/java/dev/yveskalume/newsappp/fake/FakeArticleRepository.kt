package dev.yveskalume.newsappp.fake

import dev.yveskalume.newsapp.data.repository.ArticleRepository
import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.Source

/**
 * Fake implementation of ArticleRepository that returns predefined success results.
 */
class FakeArticleRepositorySuccess(

) : ArticleRepository {

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> {
        return Result.success(sampleArticles)
    }

    companion object {
        val sampleArticles = listOf(
            Article(
                source = Source(id = "bbc-news", name = "BBC News"),
                author = "John Doe",
                title = "Sample Article 1",
                description = "This is a sample article description",
                url = "https://example.com/article1",
                urlToImage = "https://example.com/image1.jpg",
                publishedAt = "2026-01-12T10:00:00Z",
                content = "Sample article content 1"
            ),
            Article(
                source = Source(id = "cnn", name = "CNN"),
                author = "Jane Smith",
                title = "Sample Article 2",
                description = "Another sample article description",
                url = "https://example.com/article2",
                urlToImage = "https://example.com/image2.jpg",
                publishedAt = "2026-01-12T11:00:00Z",
                content = "Sample article content 2"
            ),
            Article(
                source = Source(id = "bbc-news", name = "BBC News"),
                author = "Bob Wilson",
                title = "Sample Article 3",
                description = "Third sample article description",
                url = "https://example.com/article3",
                urlToImage = "https://example.com/image3.jpg",
                publishedAt = "2026-01-12T12:00:00Z",
                content = "Sample article content 3"
            )
        )

        val page2Articles = listOf(
            Article(
                source = Source(id = "bbc-news", name = "BBC News"),
                author = "Alice Brown",
                title = "Page 2 Article 1",
                description = "Page 2 article description",
                url = "https://example.com/article4",
                urlToImage = "https://example.com/image4.jpg",
                publishedAt = "2026-01-12T13:00:00Z",
                content = "Page 2 article content 1"
            )
        )
    }
}

/**
 * Fake implementation of ArticleRepository that returns predefined failure results.
 */
class FakeArticleRepositoryFailure(
    private val errorMessage: String = "Network error: Unable to fetch articles"
) : ArticleRepository {

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> {
        return Result.failure(Exception(errorMessage))
    }
}

/**
 * Configurable fake that can switch between success and failure modes.
 */
class FakeArticleRepositoryConfigurable : ArticleRepository {

    var shouldFail: Boolean = false
    var errorMessage: String = "Network error"
    var articles: List<Article> = FakeArticleRepositorySuccess.sampleArticles
    var page2Articles: List<Article> = FakeArticleRepositorySuccess.page2Articles

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> {
        return if (shouldFail) {
            Result.failure(Exception(errorMessage))
        } else {
            val articlesToReturn = when (page) {
                1 -> articles
                2 -> page2Articles
                else -> emptyList()
            }
            Result.success(articlesToReturn)
        }
    }
}
