package dev.yveskalume.newsapp.data.network.datasource

import dev.yveskalume.newsappp.data.network.model.NewsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class NewsDataSourceImpl(
    private val httpClient: HttpClient,
) : NewsDataSource {

    override suspend fun getTopHeadlines(
        query: String?,
        sources: String?,
        pageSize: Int,
        page: Int
    ): NewsResponseDto {
        return httpClient.get("top-headlines") {
            parameter("pageSize", pageSize)
            parameter("page", page)
            query?.let { parameter("q", it) }
            sources?.let { parameter("sources", it) }
        }.body()
    }
}
