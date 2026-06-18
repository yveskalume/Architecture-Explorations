package dev.yveskalume.newsappp.ui.preview

import dev.yveskalume.newsapp.domain.model.Article
import dev.yveskalume.newsapp.domain.model.Source
import dev.yveskalume.newsapp.domain.model.SourceItem


internal object PreviewSampleData {

    val sampleSources: List<SourceItem> = listOf(
        SourceItem(
            id = "bbc-news",
            name = "BBC News",
            description = "BBC World News",
            url = "https://www.bbc.com/news",
            category = "general",
            language = "en",
            country = "gb"
        ),
        SourceItem(
            id = "cnn",
            name = "CNN",
            description = "Cable News Network",
            url = "https://www.cnn.com",
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

    val sampleArticles: List<Article> = listOf(
        Article(
            source = Source(id = "bbc-news", name = "BBC News"),
            author = "John Doe",
            title = "Breaking: Major Tech Company Announces Revolutionary Product",
            description = "A leading technology company has unveiled a groundbreaking new product that promises to transform the industry.",
            url = "https://example.com/article1",
            urlToImage = "https://picsum.photos/800/400",
            publishedAt = "2026-01-12T10:30:00Z",
            content = "Full article content here..."
        ),
        Article(
            source = Source(id = "cnn", name = "CNN"),
            author = "Jane Smith",
            title = "Global Climate Summit Reaches Historic Agreement",
            description = "World leaders have come together to sign a landmark climate accord aimed at reducing carbon emissions.",
            url = "https://example.com/article2",
            urlToImage = "https://picsum.photos/800/401",
            publishedAt = "2026-01-12T09:15:00Z",
            content = "Full article content here..."
        ),
        Article(
            source = Source(id = "techcrunch", name = "TechCrunch"),
            author = "Alex Johnson",
            title = "Startup Raises $100M in Series B Funding",
            description = "An innovative AI startup has secured significant funding to expand its operations globally.",
            url = "https://example.com/article3",
            urlToImage = "https://picsum.photos/800/402",
            publishedAt = "2026-01-12T08:00:00Z",
            content = "Full article content here..."
        )
    )
}

