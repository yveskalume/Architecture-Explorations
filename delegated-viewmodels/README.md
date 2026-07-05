# Delegated ViewModel Architecture

A single `ViewModel` is usually enough and is often the best choice. The problem appears when a screen becomes large enough that the `ViewModel` is no longer only describing the screen, but absorbing every small decision from every component: form changes, validation, formatting, bottom sheet state, and even small component visibility rules.

The [state-machine](https://github.com/yveskalume/Architecture-Explorations/tree/main/state-machine) sample tries to solve a similar problem, but it takes a different direction. It uses events, reducers, and a state manager to make state transitions explicit and deterministic. That approach gives strong separation, but it also introduces more architectural machinery.

Delegated ViewModel Architecture is less formal than the state-machine approach. It keeps the `ViewModel` as the screen owner, but delegates component behavior to smaller screen-specific controllers.

## The Core Idea

The screen is still represented by a `ViewModel`. The route composable gets the `ViewModel`, collects the state, and passes the state plus the required behavior contracts to the screen. The main difference is that the `ViewModel` does not directly contain all the screen logic anymore.

Each screen component owns a `Controller` contract. We can think of a controller contract as the public behavior API of a component. For example, the sources section can have a `SourcesController`, the articles section can have an `ArticlesController`, etc.

One of the biggest challenges with this kind of architecture is sharing data between different components without creating duplicated requests or race conditions. For example, the sources section and the articles section may both depend on the selected source. If each controller owns its own independent state or loads its own version of the data, the screen can easily become inconsistent.

To avoid this, controllers do not own independent screen state. They all update the same shared screen state through the `StateStore`. The `StateStore` becomes the single source of truth for the screen.

When two or more controllers share the same reusable logic, that logic can be extracted into a `Logic` class.

A `Logic` class can contain reusable rules such as validation or state transformation. The controller remains responsible for reacting to the user action, while the logic class contains the reusable decision-making.

Another important rule is that two components that need the same backend data should not make two separate requests. Shared initial data should be loaded once by a screen-level loader, that can be called by the `ViewModel` when the screen state starts being observed. That loader can call the repositories, receive the data, and update the shared state through the `StateStore`.

The goal is to keep the `ViewModel` lightweight without losing a single clear screen owner.

## Implementation

The `ViewModel` can own a grouped object called `HomeControllers`. This object gathers all screen controllers in one place and implements their contracts through delegation.

```kotlin
class HomeControllers(
    sourcesController: SourcesController,
    articlesController: ArticlesController,
    refreshController: RefreshController,
) : SourcesController by sourcesController,
    ArticlesController by articlesController,
    RefreshController by refreshController,
```

The `ViewModel` then exposes this grouped object.

```kotlin
class HomeViewModel(
    private val stateStore: HomeStateStore,
    val controllers: HomeControllers,
    private val initialHomeLogic: InitialHomeLogic,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        stateStore.state
            .onStart {
                initialHomeLogic.load()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.initial(),
            )
}
```

This keeps the `ViewModel` small without forcing it to manually forward every function call. The `ViewModel` owns the screen state pipeline and exposes the grouped controllers, while the actual behavior lives inside the screen-specific controllers.

The route composable can then pass the grouped controllers to the screen.

```kotlin
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        controllers = viewModel.controllers,
    )
}
```

## State and StateStore

If every controller owned its own independent state, the screen could become inconsistent. The sources controller could have one selected source, the articles controller could use another selected source, and refresh could reload data using stale values.

To avoid that, controllers do not own independent screen state. They update one shared state through the `HomeStateStore`.

The `HomeStateStore` is the single owner of the mutable screen state.

```kotlin
class HomeStateStore {

    private val _state = MutableStateFlow(HomeUiState.initial())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun update(reducer: HomeUiState.() -> HomeUiState) {
        _state.update { current ->
            current.reducer()
        }
    }

    fun updateSourcesSelection(source: SourceItem?) {
        update {
            val updatedSourcesUiState = when (val current = sourcesUiState) {
                is SourcesUiState.Success -> current.copy(selected = source)
                else -> current
            }

            copy(
                selectedSource = source,
                sourcesUiState = updatedSourcesUiState,
            )
        }
    }

    fun setSourcesLoading() {
        update {
            copy(sourcesUiState = SourcesUiState.Loading)
        }
    }
}
```

It can also expose explicit functions for common state changes. This makes the allowed state transitions easier to discover.

## Screen State

The screen has one global UI state. For the Home screen, this can be called `HomeUiState`.

```kotlin
data class HomeUiState(
    val selectedSource: SourceItem?,
    val sourcesUiState: SourcesUiState,
    val articlePageSnapshot: PageSnapshot<Article>,
    val refreshUiState: RefreshUiState,
) {
    companion object {
        fun initial() = HomeUiState(
            selectedSource = null,
            sourcesUiState = SourcesUiState.Loading,
            articlePageSnapshot = PageSnapshot(),
            refreshUiState = RefreshUiState.Idle,
        )
    }

    val isLoading: Boolean
        get() = sourcesUiState is SourcesUiState.Loading &&
            articlePageSnapshot.dataState.isLoading()

    val error: String?
        get() {
            val sourcesError = sourcesUiState as? SourcesUiState.Error
            val articlesError = articlePageSnapshot.dataState as? DataState.Error

            return when {
                sourcesError != null && articlesError != null -> articlesError.message
                else -> null
            }
        }
}
```

## Controllers

A controller owns the behavior of one part of the screen.

For example, `SourcesController` describes what the UI can do with sources.

```kotlin
interface SourcesController {
    fun selectSource(source: SourceItem?)
}
```

The implementation belongs to the Home screen.

```kotlin
class HomeSourcesController(
    private val scope: CoroutineScope,
    private val stateStore: HomeStateStore,
    private val getArticleLogic: GetArticleLogic,
) : SourcesController {

    private var selectSourceJob: Job? = null

    override fun selectSource(source: SourceItem?) {
        val currentSource = stateStore.state.value.selectedSource

        val selectedSource = source.takeIf {
            it?.id != currentSource?.id
        }

        stateStore.updateSourcesSelection(selectedSource)

        selectSourceJob?.cancel()
        selectSourceJob = scope.launch {
            stateStore.setArticlesLoading(reset = true)

            getArticleLogic.load(
                snapshot = stateStore.currentArticlePageSnapshot(),
                sourceId = selectedSource?.id,
                page = PageNumber(1),
            ).onSuccess(stateStore::updateArticlePageSnapshot)
                .onFailure { error ->
                    stateStore.setArticlesError(error.message)
                }
        }
    }
}
```

## Logic

When two or more controllers share the same reusable logic, that logic can be extracted into a `Logic` class.

A good example is search. Search logic can quickly appear in different places. A search box controller may need to update the query, clean it, and decide whether the search button should be enabled. Another controller may need to clear the query or restore a previous search. Instead of duplicating that logic in several controllers, we can move it to a `SearchLogic`.

```kotlin
class SearchLogic {
    fun normalizeQuery(query: String): String {
        return query.trim()
    }

    fun canSearch(query: String): Boolean {
        return normalizeQuery(query).isNotBlank()
    }
}
```

The controller remains responsible for reacting to the user action, while the `Logic` class contains the reusable logic.

```kotlin
class SearchScreenController(
    private val stateStore: SearchStateStore,
    private val searchLogic: SearchLogic,
) : SearchController {

    override fun onQueryChanged(query: String) {
        stateStore.setQuery(query)

        val normalizedQuery = searchLogic.normalizeQuery(query)
        if (!searchLogic.canSearch(normalizedQuery)) {
            stateStore.clearArticles()
            return
        }

        // Load articles for the normalized query.
    }

    override fun clearSearch() {
        stateStore.setQuery("")
        stateStore.clearArticles()
    }

    override fun loadMore() {
        // Load the next article page.
    }
}
```


I think a `Logic` should not mutate state directly. It should receive values and return the result of applying reusable logic.

## Composable Design

The screen composable receives the grouped controllers object.

```kotlin
@Composable
fun HomeScreen(
    state: HomeUiState,
    controllers: HomeControllers,
) {
    SourcesSection(
        state = state.sourcesUiState,
        controller = controllers,
    )

    ArticlesSection(
        articlePageSnapshot = state.articlePageSnapshot,
        controller = controllers,
    )
}
```

Each section receives only the state and controller contract it needs.

```kotlin
@Composable
fun SourcesSection(
    state: SourcesUiState,
    controller: SourcesController,
) {
    when (state) {
        SourcesUiState.Loading -> SourcesRowShimmer()

        is SourcesUiState.Success -> SourcesRow(
            sources = state.sources,
            selectedSource = state.selected,
            onSourceClick = controller::selectSource,
        )

        is SourcesUiState.Error -> Unit
    }
}
```


The result is a screen architecture that keeps the comfort of MVVM while making large screens easier to organize.
