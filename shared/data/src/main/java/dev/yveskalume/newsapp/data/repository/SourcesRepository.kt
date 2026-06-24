package dev.yveskalume.newsapp.data.repository

import dev.yveskalume.newsapp.domain.model.SourceItem

interface SourcesRepository {
    suspend fun getSources(
        category: String? = null
    ): Result<List<SourceItem>>
}
