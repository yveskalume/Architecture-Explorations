package dev.yveskalume.newsappp.data.repository

import dev.yveskalume.newsappp.domain.model.SourceItem

interface SourcesRepository {
    suspend fun getSources(
        category: String? = null
    ): Result<List<SourceItem>>
}
