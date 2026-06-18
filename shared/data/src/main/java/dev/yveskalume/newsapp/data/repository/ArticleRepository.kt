package dev.yveskalume.newsappp.data.repository

import dev.yveskalume.newsappp.domain.model.Article

interface ArticleRepository {
    /**
     * Fetches top headlines
     * @param query Keywords or a phrase to search for (optional)
     * @param sources News source id (optional, e.g., "bbc-news")
     * @param pageSize Number of results per page (default 20)
     * @param page Page number (default 1)
     */
    suspend fun getTopHeadlines(
        query: String? = null,
        sources: String? = null,
        pageSize: Int = 20,
        page: Int = 1
    ): Result<List<Article>>
}
