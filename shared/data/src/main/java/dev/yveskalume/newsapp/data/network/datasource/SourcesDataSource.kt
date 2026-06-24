package dev.yveskalume.newsapp.data.network.datasource

import dev.yveskalume.newsapp.data.network.model.SourcesResponseDto

interface SourcesDataSource {
    suspend fun getSources(
        category: String? = null
    ): SourcesResponseDto
}
