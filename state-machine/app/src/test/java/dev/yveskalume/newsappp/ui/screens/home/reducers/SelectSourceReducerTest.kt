package dev.yveskalume.newsappp.ui.screens.home.reducers

import dev.yveskalume.newsappp.fake.FakeSourcesRepositorySuccess
import dev.yveskalume.newsappp.ui.screens.home.HomeEvent
import dev.yveskalume.newsappp.ui.screens.home.HomeUiState
import dev.yveskalume.newsappp.ui.screens.home.SourcesUiState
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SelectSourceReducerTest : ShouldSpec({

    should("select a source when it is different from current") {
        runTest {
            val reducer = SelectSourceReducer()
            val source = FakeSourcesRepositorySuccess.sampleSources.first()
            val state = HomeUiState.initial().copy(
                sourcesUiState = SourcesUiState.Success(FakeSourcesRepositorySuccess.sampleSources)
            )

            val result = reducer.reduce(state, HomeEvent.SelectSource(source))

            result.selectedSource shouldBe source
            (result.sourcesUiState as SourcesUiState.Success).selected shouldBe source
        }
    }

    should("clear selected source when selecting the same source again") {
        runTest {
            val reducer = SelectSourceReducer()
            val source = FakeSourcesRepositorySuccess.sampleSources.first()
            val state = HomeUiState.initial().copy(
                sourcesUiState = SourcesUiState.Success(FakeSourcesRepositorySuccess.sampleSources)
            )

            val selected = reducer.reduce(state, HomeEvent.SelectSource(source))
            val result = reducer.reduce(selected, HomeEvent.SelectSource(source))

            result.selectedSource shouldBe null
            (result.sourcesUiState as SourcesUiState.Success).selected shouldBe null
        }
    }

    should("request article reload after selection changes") {
        runTest {
            val publishedEvents = mutableListOf<HomeEvent>()
            val reducer = SelectSourceReducer(onPublishEvent = publishedEvents::add)
            val source = FakeSourcesRepositorySuccess.sampleSources.first()
            val state = HomeUiState.initial().copy(
                sourcesUiState = SourcesUiState.Success(FakeSourcesRepositorySuccess.sampleSources)
            )

            reducer.reduce(state, HomeEvent.SelectSource(source))

            publishedEvents shouldBe listOf(HomeEvent.LoadArticles())
        }
    }
})
