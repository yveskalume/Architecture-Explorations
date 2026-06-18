package dev.yveskalume.newsapp.data.repository

import dev.yveskalume.newsapp.data.network.datasource.NewsDataSource
import dev.yveskalume.newsapp.data.network.model.toDomain
import dev.yveskalume.newsapp.domain.model.Article

class ArticleRepositoryImpl(
    private val newsDataSource: NewsDataSource
) : ArticleRepository {

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): Result<List<Article>> = runCatching {
        newsDataSource.getTopHeadlines(
            query = query,
            sources = sources,
            pageSize = pageSize,
            page = page
        ).toDomain()
    }
}
