package dev.yveskalume.newsapp.data.network.datasource

import dev.yveskalume.newsapp.data.network.model.SourcesResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SourcesDataSourceImpl(
    private val httpClient: HttpClient,
) : SourcesDataSource {
    override suspend fun getSources(category: String?): SourcesResponseDto {
        return httpClient.get("top-headlines/sources") {
            category?.let { parameter("category", it) }
        }.body()
    }
}
