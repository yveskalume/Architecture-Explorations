package dev.yveskalume.newsappp.fake

import dev.yveskalume.newsapp.data.repository.SourcesRepository
import dev.yveskalume.newsapp.domain.model.SourceItem

/**
 * Fake implementation of SourcesRepository that returns predefined success results.
 */
class FakeSourcesRepositorySuccess(
    private val sourcesProvider: (category: String?) -> List<SourceItem> = { _ -> sampleSources }
) : SourcesRepository {

    override suspend fun getSources(category: String?): Result<List<SourceItem>> {
        return Result.success(sourcesProvider(category))
    }

    companion object {
        val sampleSources = listOf(
            SourceItem(
                id = "bbc-news",
                name = "BBC News",
                description = "BBC World News",
                url = "https://bbc.com",
                category = "general",
                language = "en",
                country = "gb"
            ),
            SourceItem(
                id = "cnn",
                name = "CNN",
                description = "Cable News Network",
                url = "https://cnn.com",
                category = "general",
                language = "en",
                country = "us"
            ),
            SourceItem(
                id = "techcrunch",
                name = "TechCrunch",
                description = "Technology news",
                url = "https://techcrunch.com",
                category = "technology",
                language = "en",
                country = "us"
            )
        )
    }
}

/**
 * Fake implementation of SourcesRepository that returns predefined failure results.
 */
class FakeSourcesRepositoryFailure(
    private val errorMessage: String = "Network error: Unable to fetch sources"
) : SourcesRepository {

    override suspend fun getSources(category: String?): Result<List<SourceItem>> {
        return Result.failure(Exception(errorMessage))
    }
}

/**
 * Configurable fake that can switch between success and failure modes.
 */
class FakeSourcesRepositoryConfigurable : SourcesRepository {

    var shouldFail: Boolean = false
    var errorMessage: String = "Network error"
    var sources: List<SourceItem> = FakeSourcesRepositorySuccess.sampleSources

    override suspend fun getSources(category: String?): Result<List<SourceItem>> {
        return if (shouldFail) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(sources)
        }
    }
}
