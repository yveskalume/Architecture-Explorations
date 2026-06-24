# Event Driven (State Machine) Architecture

This module uses an event + reducer state machine approach with deterministic state transitions.

Instead of putting most logic inside one ViewModel with many callbacks, the UI sends events and reducers handle deterministic transitions.

## Event Processing Behavior

Reducers may publish follow-up events through an `onPublishEvent(...)` callback. For example, source selection updates the selected source and publishes `HomeEvent.LoadArticles()` so the article list reloads for the new source.

Even when `onPublishEvent(HomeEvent.LoadArticles())` is called before the reducer returns, the follow-up event is queued through `StateManager.onEvent(...)`. The current reducer still returns the next `HomeUiState`, and `StateManager` applies that state before the queued follow-up event is processed. This means the follow-up reducer reads the updated state.

This is an intentional architecture behavior in this sample: reducers can describe a transition and enqueue the next event in the same reducer.

## Pros
- Fewer callbacks in Compose screens: UI mostly sends events (`onEvent(...)`) instead of passing many lambdas.
- No giant `when` block in ViewModel to handle all intents.
- Reducers are self-contained pieces of logic, so responsibilities stay focused.
- Reducers are easy to unit test in isolation.
- Lower cognitive load when reading feature behavior: each deterministic event path lives in a dedicated reducer.
- More predictable and deterministic state updates, since transitions happen through explicit events.

## Cons
- More files: each feature can end up with many event/state/reducer files.
- Heavier DI dependency: reducer discovery is tied to Koin scope/qualifiers.
- Migrating DI frameworks (for example Koin to Hilt/manual DI) can be costly.
- Harder onboarding for developers unfamiliar with event-driven architecture.

## When It Is a Good Fit
- Feature complexity is growing.
- ViewModels are becoming large and hard to maintain.
- You want stronger testability and deterministic, clearer state transitions.

## When MVVM May Be Better
- Small/simple screens.
- Teams that prefer minimal abstraction and fewer files.
- Projects where DI/framework coupling must stay very low.
