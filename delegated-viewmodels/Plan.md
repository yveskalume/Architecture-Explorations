# Delegated ViewModel Architecture

A single `ViewModel` is usually enough and is often the best choice. The problem appears when a screen becomes large enough that the `ViewModel` is no longer only describing the screen, but absorbing every small decision from every component: form changes, validation, formatting, bottom sheet state, and even the smallest component visibility logic.

The [state-machine](https://github.com/yveskalume/Architecture-Explorations/tree/main/state-machine) sample tries to solve a similar problem, but it takes a different direction. It uses events, reducers, and a state manager to make state transitions explicit and deterministic. That approach gives strong separation, but it also introduces more architectural machinery.

Delegated ViewModel Architecture is less formal than the state-machine approach. It keeps the `ViewModel` as the screen owner, but delegates component behavior to smaller screen-specific interactors.

## The Core Idea

The screen is still represented by a `ViewModel`. The route composable gets the `ViewModel`, collects the state, and passes the state plus the required behavior contracts to the screen. The main difference is that the `ViewModel` does not directly contain all the screen logic anymore.

Each screen component owns an `Interactor` contract. We can think of an interactor contract as the public behavior API of a component. For example, the sources section can have a `SourcesInteractor`, the articles section can have an `ArticlesInteractor`, and the refresh behavior can have a `RefreshInteractor`.

The `ViewModel` exposes all those interactors through a grouped object called `HomeInteractors`. This keeps the screen with one clear entry point while keeping the behavior split into smaller classes.

One of the biggest challenges with this kind of architecture is sharing data between different components without creating duplicated requests or race conditions. For example, the sources section and the articles section may both depend on the selected source. If each interactor owns its own independent state or loads its own version of the data, the screen can easily become inconsistent.

To avoid this, interactors do not own independent state. They all update the same shared screen state through the `StateHandler`. The `StateHandler` becomes the single source of truth for the screen.

When two or more interactors share the same logic, that logic can be extracted into a `Behaviour` class. We could also call these classes use cases, but in this architecture we keep the word `Behaviour` to describe reusable screen or UI behavior.

A `Behaviour` can contain logic such as formatting, validation, normalization, visibility rules, or shared state transformation. The interactor remains responsible for reacting to the user action.

Another important rule is that two components that need the same backend data should not make two separate requests. Shared initial data should be loaded once by a screen-level behaviour, called by the `ViewModel` when the screen state starts being observed. That behaviour can call the repositories, receive the data, and update the shared state through the `StateHandler`.

```txt
ViewModel
    exposes state
    exposes grouped interactors
    starts shared initial loading

HomeInteractors
    groups all interactor contracts

Interactors
    handle component behavior
    update state through StateHandler

StateHandler
    owns the mutable screen state

Behaviours
    contain reusable logic shared by interactors

Repositories
    provide business logic, backend data, or cached data
```

The goal is to keep the `ViewModel` lightweight without losing a single clear screen owner.

## Implementation

The ViewModel can own a grouped object called `HomeInteractors`. This object gathers all screen interactors in one place and implements their contracts through delegation.

```kotlin
class HomeInteractors(
    sourcesInteractor: SourcesInteractor,
    articlesInteractor: ArticlesInteractor,
    refreshInteractor: RefreshInteractor,
    searchInteractor: SearchInteractor,
) : SourcesInteractor by sourcesInteractor,
    ArticlesInteractor by articlesInteractor,
    RefreshInteractor by refreshInteractor,
    SearchInteractor by searchInteractor
```

The `ViewModel` then exposes this grouped object.

```kotlin
class HomeViewModel(
    private val stateHandler: HomeStateHandler,
    val interactors: HomeInteractors,
    private val initialHomeBehaviour: InitialHomeBehaviour,
) : ViewModel() {

    val state: StateFlow<HomeUiState> =
        stateHandler.state
            .onStart {
                initialHomeBehaviour.load()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.initial(),
            )
}
```

This keeps the `ViewModel` small without forcing it to manually forward every function call. The `ViewModel` owns the screen state pipeline and exposes the grouped interactors, while the actual behavior lives inside the screen-specific interactors.

The route composable can then pass the grouped interactors to the screen.

```kotlin
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        interactors = viewModel.interactors,
    )
}
```

The interactors are injected directly by Koin. They are not created manually by the `ViewModel` and they do not need a factory.

When an interactor needs to launch coroutines, it receives a `CoroutineScope` from the same Koin scope as the interactor. That scope is created for the Home feature scope and should be closed with that scope. This keeps the coroutine work lifecycle-aware without making the `ViewModel` responsible for wiring every interactor.

```kotlin
const val HOME_DELEGATED_VIEWMODEL_SCOPE = "HOME_DELEGATED_VIEWMODEL_SCOPE"

val homeDelegatedViewModelModule = module {

    scope(named(HOME_DELEGATED_VIEWMODEL_SCOPE)) {

        scoped<CoroutineScope> {
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        } onClose { scope ->
            scope?.cancel()
        }

        scoped {
            HomeStateHandler()
        }

        scoped {
            InitialHomeBehaviour(
                sourcesRepository = get(),
                articleRepository = get(),
                stateHandler = get(),
            )
        }

        scoped {
            SearchBehaviour()
        }

        scoped<SourcesInteractor> {
            HomeSourcesInteractor(
                scope = get(),
                stateHandler = get(),
                articleRepository = get(),
            )
        }

        scoped<ArticlesInteractor> {
            HomeArticlesInteractor(
                scope = get(),
                stateHandler = get(),
                articleRepository = get(),
            )
        }

        scoped<RefreshInteractor> {
            HomeRefreshInteractor(
                scope = get(),
                stateHandler = get(),
                sourcesRepository = get(),
                articleRepository = get(),
            )
        }

        scoped<SearchInteractor> {
            HomeSearchInteractor(
                stateHandler = get(),
                searchBehaviour = get(),
            )
        }

        scoped {
            HomeInteractors(
                sourcesInteractor = get(),
                articlesInteractor = get(),
                refreshInteractor = get(),
                searchInteractor = get(),
            )
        }

        viewModel {
            HomeViewModel(
                stateHandler = get(),
                interactors = get(),
                initialHomeBehaviour = get(),
            )
        }
    }
}
```

The important rule is that the `CoroutineScope` is not injected as an application-wide dependency. It belongs to the same Koin scope as the screen dependencies. The interactors, behaviours, `StateHandler`, and grouped `HomeInteractors` object all live in that same scope.

```txt
Koin scope
    owns CoroutineScope
    owns StateHandler
    owns Behaviours
    owns Interactors
    owns HomeInteractors

ViewModel
    receives StateHandler
    receives HomeInteractors
    receives InitialHomeBehaviour

Composable
    receives state
    receives viewModel.interactors
```

## State and StateHandler

If every interactor owned its own independent state, the screen could become inconsistent. The sources interactor could have one selected source, the articles interactor could use another selected source, and refresh could reload data using stale values.

To avoid that, interactors do not own independent screen state. They update one shared state through the `HomeStateHandler`.

The `HomeStateHandler` is the single owner of the mutable screen state.

```kotlin
class HomeStateHandler {

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

    fun setSourcesSuccess(sources: List<SourceItem>) {
        update {
            copy(
                sourcesUiState = SourcesUiState.Success(
                    sources = sources,
                    selected = selectedSource,
                )
            )
        }
    }

    fun setSourcesError(message: String?) {
        update {
            copy(
                sourcesUiState = SourcesUiState.Error(
                    message = message ?: "Failed to load sources"
                )
            )
        }
    }

    fun setArticlesLoading() {
        update {
            copy(articlesUiState = ArticlesUiState.Loading)
        }
    }

    fun setArticlesSuccess(articles: List<Article>) {
        update {
            copy(articlesUiState = ArticlesUiState.Success(articles))
        }
    }

    fun setArticlesError(message: String?) {
        update {
            copy(
                articlesUiState = ArticlesUiState.Error(
                    message = message ?: "Failed to load news"
                )
            )
        }
    }

    fun setRefreshLoading(isLoading: Boolean) {
        update {
            copy(
                refreshUiState = if (isLoading) {
                    RefreshUiState.Refreshing
                } else {
                    RefreshUiState.Idle
                }
            )
        }
    }

    fun updateSearch(
        reducer: SearchUiState.() -> SearchUiState,
    ) {
        update {
            copy(searchUiState = searchUiState.reducer())
        }
    }
}
```

The `StateHandler` should not fetch data. It should not call repositories. It should not contain heavy business logic. Its role is to make state mutation centralized, safe, and readable.

It can also expose explicit functions for common state changes. This makes the allowed state transitions easier to discover.

## Screen State

The screen has one global UI state. For the Home screen, this can be called `HomeUiState`.

```kotlin
data class HomeUiState(
    val selectedSource: SourceItem?,
    val sourcesUiState: SourcesUiState,
    val articlesUiState: ArticlesUiState,
    val refreshUiState: RefreshUiState,
    val searchUiState: SearchUiState,
) {
    companion object {
        fun initial() = HomeUiState(
            selectedSource = null,
            sourcesUiState = SourcesUiState.Loading,
            articlesUiState = ArticlesUiState.Loading,
            refreshUiState = RefreshUiState.Idle,
            searchUiState = SearchUiState(),
        )
    }

    val isLoading: Boolean
        get() = sourcesUiState is SourcesUiState.Loading &&
            articlesUiState is ArticlesUiState.Loading

    val error: String?
        get() {
            val sourcesError = sourcesUiState as? SourcesUiState.Error
            val articlesError = articlesUiState as? ArticlesUiState.Error

            return when {
                sourcesError != null && articlesError != null -> articlesError.message
                else -> null
            }
        }
}
```

The global state is composed of smaller state slices. A state slice represents the state of one part of the screen.

```kotlin
sealed interface SourcesUiState {
    data object Loading : SourcesUiState

    data class Success(
        val sources: List<SourceItem>,
        val selected: SourceItem?,
    ) : SourcesUiState

    data class Error(
        val message: String,
    ) : SourcesUiState
}
```

The article section can also have its own sealed state.

```kotlin
sealed interface ArticlesUiState {
    data object Loading : ArticlesUiState

    data class Success(
        val articles: List<Article>,
    ) : ArticlesUiState

    data class Error(
        val message: String,
    ) : ArticlesUiState
}
```

Refresh can stay simple.

```kotlin
sealed interface RefreshUiState {
    data object Idle : RefreshUiState
    data object Refreshing : RefreshUiState
}
```

Search can be represented as a simple data class.

```kotlin
data class SearchUiState(
    val query: String = "",
    val canSearch: Boolean = false,
)
```

Global loading and global error should be derived from state slices when possible.

A full-screen loading state should mean that the whole screen cannot render yet. A global error should mean that the whole screen has failed. Partial failures should stay inside their component state.

This keeps the screen flexible. If sources fail but articles can still render, the screen can show articles and handle the source error locally. If articles fail but sources are available, the user can still see the sources row and retry the article section.

## Interactors

An interactor owns the behavior of one part of the screen.

For example, `SourcesInteractor` describes what the UI can do with sources.

```kotlin
interface SourcesInteractor {
    fun selectSource(source: SourceItem?)
}
```

The implementation belongs to the Home screen.

```kotlin
class HomeSourcesInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: HomeStateHandler,
    private val articleRepository: ArticleRepository,
) : SourcesInteractor {

    private var selectSourceJob: Job? = null

    override fun selectSource(source: SourceItem?) {
        val currentSource = stateHandler.state.value.selectedSource

        val selectedSource = source.takeIf {
            it?.id != currentSource?.id
        }

        stateHandler.updateSourcesSelection(selectedSource)

        selectSourceJob?.cancel()
        selectSourceJob = scope.launch {
            stateHandler.setArticlesLoading()

            articleRepository.getTopHeadlines(
                sources = selectedSource?.id,
            ).onSuccess { articles ->
                stateHandler.setArticlesSuccess(articles)
            }.onFailure { error ->
                stateHandler.setArticlesError(error.message)
            }
        }
    }
}
```

This example intentionally stays simple. When the user selects a source, the interactor updates the selected source in the shared state, fetches articles for that source, and updates the article state with the result.

The previous request is cancelled before a new one starts. This prevents an older request from finishing after a newer one and overriding the screen with outdated articles.

The important point is that the interactor can use a repository directly when the behavior is specific to that component. It does not need to call another interactor or introduce a coordinator for every case.

The articles interactor can own article-specific behavior.

```kotlin
interface ArticlesInteractor {
    fun retry()
}
```

```kotlin
class HomeArticlesInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: HomeStateHandler,
    private val articleRepository: ArticleRepository,
) : ArticlesInteractor {

    private var retryJob: Job? = null

    override fun retry() {
        retryJob?.cancel()
        retryJob = scope.launch {
            stateHandler.setArticlesLoading()

            articleRepository.getTopHeadlines(
                sources = stateHandler.state.value.selectedSource?.id,
            ).onSuccess { articles ->
                stateHandler.setArticlesSuccess(articles)
            }.onFailure { error ->
                stateHandler.setArticlesError(error.message)
            }
        }
    }
}
```

Refresh can own refresh-specific behavior.

```kotlin
interface RefreshInteractor {
    fun refresh()
}
```

```kotlin
class HomeRefreshInteractor(
    private val scope: CoroutineScope,
    private val stateHandler: HomeStateHandler,
    private val sourcesRepository: SourcesRepository,
    private val articleRepository: ArticleRepository,
) : RefreshInteractor {

    private var refreshJob: Job? = null

    override fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            stateHandler.setRefreshLoading(true)

            try {
                sourcesRepository.getSources()
                    .onSuccess { sources ->
                        stateHandler.setSourcesSuccess(sources)
                    }
                    .onFailure { error ->
                        stateHandler.setSourcesError(error.message)
                    }

                articleRepository.getTopHeadlines(
                    sources = stateHandler.state.value.selectedSource?.id,
                ).onSuccess { articles ->
                    stateHandler.setArticlesSuccess(articles)
                }.onFailure { error ->
                    stateHandler.setArticlesError(error.message)
                }
            } finally {
                stateHandler.setRefreshLoading(false)
            }
        }
    }
}
```

## Behaviours

When two or more interactors share the same logic, that logic can be extracted into a `Behaviour` class.

A `Behaviour` is not a repository. It is not the owner of screen state. It is a reusable logic holder.

A good example is search. Search logic can quickly appear in different places. A search box interactor may need to update the query, clean it, and decide whether the search button should be enabled. Another interactor may need to clear the query or restore a previous search. Instead of duplicating that logic in several interactors, we can move it to a `SearchBehaviour`.

```kotlin
class SearchBehaviour {

    fun normalizeQuery(query: String): String {
        return query.trim()
    }

    fun canSearch(query: String): Boolean {
        return normalizeQuery(query).length >= MIN_QUERY_LENGTH
    }

    fun updateQuery(
        currentState: SearchUiState,
        query: String,
    ): SearchUiState {
        val normalizedQuery = normalizeQuery(query)

        return currentState.copy(
            query = query,
            canSearch = canSearch(normalizedQuery),
        )
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 2
    }
}
```

The interactor remains responsible for reacting to the user action, while the `Behaviour` contains the reusable logic.

```kotlin
interface SearchInteractor {
    fun onQueryChanged(query: String)
    fun clearSearch()
}
```

```kotlin
class HomeSearchInteractor(
    private val stateHandler: HomeStateHandler,
    private val searchBehaviour: SearchBehaviour,
) : SearchInteractor {

    override fun onQueryChanged(query: String) {
        stateHandler.updateSearch {
            searchBehaviour.updateQuery(
                currentState = this,
                query = query,
            )
        }
    }

    override fun clearSearch() {
        stateHandler.updateSearch {
            searchBehaviour.updateQuery(
                currentState = this,
                query = "",
            )
        }
    }
}
```

This gives us a clean separation.

```txt
User types query
    → SearchInteractor receives the action
    → SearchBehaviour computes the next search state
    → StateHandler updates HomeUiState
    → Composable renders the new state
```

A `Behaviour` should be easy to test in isolation. It should not depend on the `ViewModel`. It should not know about Compose. In most cases, it should not mutate state directly. It should receive values and return the result of applying reusable logic.

## Initial Loading

Two components that need the same backend data should not make two different requests for the same data.

Shared initial data should be loaded once by a screen-level `Behaviour`. The `ViewModel` calls this `Behaviour` when the screen state starts being observed. The `Behaviour` calls repositories and updates the shared state through the `StateHandler`.

The `load()` function is a suspend function because it is called from the `ViewModel` state pipeline.

```kotlin
class InitialHomeBehaviour(
    private val sourcesRepository: SourcesRepository,
    private val articleRepository: ArticleRepository,
    private val stateHandler: HomeStateHandler,
) {
    private var hasStarted = false

    suspend fun load() {
        if (hasStarted) return
        hasStarted = true

        coroutineScope {
            stateHandler.setSourcesLoading()
            stateHandler.setArticlesLoading()

            val sourcesRequest = async {
                sourcesRepository.getSources()
            }

            val articlesRequest = async {
                articleRepository.getTopHeadlines(
                    sources = stateHandler.state.value.selectedSource?.id,
                )
            }

            sourcesRequest.await()
                .onSuccess { sources ->
                    stateHandler.setSourcesSuccess(sources)
                }
                .onFailure { error ->
                    stateHandler.setSourcesError(error.message)
                }

            articlesRequest.await()
                .onSuccess { articles ->
                    stateHandler.setArticlesSuccess(articles)
                }
                .onFailure { error ->
                    stateHandler.setArticlesError(error.message)
                }
        }
    }
}
```

This makes initial loading explicit and prevents duplicated requests. It also keeps the composable free from initial loading logic.

The route composable should not do this:

```kotlin
LaunchedEffect(Unit) {
    viewModel.loadInitialData()
}
```

The composable should collect state and render it. Initial loading belongs to the `ViewModel` state pipeline.

## Composable Design

Composables render state and call interactor contracts.

The route composable gets the `ViewModel` and collects the state.

```kotlin
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        interactors = viewModel.interactors,
    )
}
```

The screen composable receives the grouped interactors object.

```kotlin
@Composable
fun HomeScreen(
    state: HomeUiState,
    interactors: HomeInteractors,
) {
    SourcesSection(
        state = state.sourcesUiState,
        interactor = interactors,
    )

    ArticlesSection(
        state = state.articlesUiState,
        interactor = interactors,
    )

    SearchSection(
        state = state.searchUiState,
        interactor = interactors,
    )
}
```

Each section receives only the state and interactor contract it needs.

```kotlin
@Composable
fun SourcesSection(
    state: SourcesUiState,
    interactor: SourcesInteractor,
) {
    when (state) {
        SourcesUiState.Loading -> SourcesRowShimmer()

        is SourcesUiState.Success -> SourcesRow(
            sources = state.sources,
            selectedSource = state.selected,
            onSourceClick = interactor::selectSource,
        )

        is SourcesUiState.Error -> Unit
    }
}
```

```kotlin
@Composable
fun ArticlesSection(
    state: ArticlesUiState,
    interactor: ArticlesInteractor,
) {
    when (state) {
        ArticlesUiState.Loading -> ArticlesLoading()

        is ArticlesUiState.Success -> ArticlesList(
            articles = state.articles,
        )

        is ArticlesUiState.Error -> ArticlesError(
            message = state.message,
            onRetry = interactor::retry,
        )
    }
}
```

```kotlin
@Composable
fun SearchSection(
    state: SearchUiState,
    interactor: SearchInteractor,
) {
    SearchInput(
        query = state.query,
        canSearch = state.canSearch,
        onQueryChanged = interactor::onQueryChanged,
        onClearClick = interactor::clearSearch,
    )
}
```

The UI should not call repositories. It should not mutate the `StateHandler`. It should not start initial loading manually.

## Dependency Direction

The `ViewModel` owns the screen state pipeline and exposes the grouped interactors object. The interactors are injected directly by Koin, together with the `StateHandler`, behaviours, and the screen `CoroutineScope`. The `ViewModel` does not create the interactors manually and does not pass `viewModelScope` to a factory.

Screen-specific interactors can depend on the `StateHandler`, repositories, and behaviours. They can launch work using the `CoroutineScope` provided by the same Koin scope. The `StateHandler` depends only on UI state and optional restoration storage. Behaviours can depend on repositories or other low-level reusable logic.

Composables depend only on state slices and interactor contracts.

```txt
Composable
    depends on UiState slices
    depends on Interactor contracts

ViewModel
    depends on StateHandler
    depends on HomeInteractors
    depends on InitialHomeBehaviour

HomeInteractors
    groups interactor contracts

Interactors
    depend on CoroutineScope
    depend on StateHandler
    depend on repositories
    depend on behaviours when needed

StateHandler
    depends on HomeUiState

Behaviours
    depend on reusable logic
    may depend on repositories when they represent shared loading behaviour
```

## Suggested Package Structure

For this sample, a screen-first structure is easier to read.

```txt
delegated-viewmodel/
    app/
        src/main/java/dev/yveskalume/newsappp/
            ui/screens/home/
                HomeScreen.kt
                HomeViewModel.kt
                HomeUiState.kt
                HomeStateHandler.kt
                HomeInteractors.kt
                HomeDiModule.kt

                interactors/
                    SourcesInteractor.kt
                    ArticlesInteractor.kt
                    RefreshInteractor.kt
                    SearchInteractor.kt

                    HomeSourcesInteractor.kt
                    HomeArticlesInteractor.kt
                    HomeRefreshInteractor.kt
                    HomeSearchInteractor.kt

                behaviours/
                    InitialHomeBehaviour.kt
                    SearchBehaviour.kt

                components/
                    SourcesSection.kt
                    ArticlesSection.kt
                    SearchSection.kt
```

The exact package names can change, but the important part is that the screen keeps a clear place for state, interactors, behaviours, and composables.

## Final Summary

Delegated ViewModel Architecture keeps the `ViewModel` as the screen owner, but avoids making it the owner of every behavior. A screen can have many interactors, so the `ViewModel` exposes a grouped `HomeInteractors` object instead of manually implementing or constructing every interactor.

The interactors are injected directly by Koin. The Koin scope owns the screen `CoroutineScope`, `StateHandler`, behaviours, interactors, and grouped `HomeInteractors`. Interactors update shared state through the `StateHandler`. The `StateHandler` owns the mutable UI state. Behaviours contain reusable logic and shared loading operations. Composables only render state and call narrow interactor contracts.

The result is a screen architecture that keeps the comfort of MVVM while making large screens easier to organize.
