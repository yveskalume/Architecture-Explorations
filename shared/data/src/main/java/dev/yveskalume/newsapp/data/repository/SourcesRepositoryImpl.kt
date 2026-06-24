package dev.yveskalume.newsapp.data.repository

import dev.yveskalume.newsapp.data.network.datasource.SourcesDataSource
import dev.yveskalume.newsapp.data.network.model.toDomain
import dev.yveskalume.newsapp.domain.model.SourceItem

class SourcesRepositoryImpl(
    private val sourcesDataSource: SourcesDataSource
) : SourcesRepository {

    override suspend fun getSources(category: String?): Result<List<SourceItem>> = runCatching {
        sourcesDataSource.getSources(category = category).toDomain()
    }
}
