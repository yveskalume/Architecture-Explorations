# NewsAppp

> **Warning**: This project is intentionally over-engineered for educational and experimental purposes. Read at your own risk!

## About

This project should probably be called “Architecture Madness”. It’s my personal excuse to over-engineer a News app, and I’m going to push every concept to its extreme version; not because it’s the smartest thing to do, but because it’s fun (because we all sometimes love complex things; even when they don’t bring much value). And since I can’t do this at work, I’m doing it here. 

Read the code at your own risk. You can still learn from these samples and steal what’s useful, but please don’t go “I did it because it does so here”. Also feel free to imitate what you see on big companies’ engineering blogs… you know, despite the tiny detail that you’re not them (and neither am I).”

## Project Structure

This repository demonstrates different architectural approaches to building the same News application:

#### MVVM Implementation
A classic MVVM (Model-View-ViewModel) approach with project-specific patterns like interface-based screen contracts, custom paging, and restartable state flow. See [mvvm/README.md](mvvm/README.md) for details.

#### Event-Driven
An event-driven architecture where UI dispatches events and reducers handle state transitions. See [state-machine/README.md](state-machine/README.md) for details.

#### Shared Gradle Build
Architecture samples stay as separate Gradle projects so they can be opened independently in Android Studio. Common Android, Kotlin, Compose, dependency, and test setup lives in [build-logic](build-logic), and all builds read versions from the root [gradle/libs.versions.toml](gradle/libs.versions.toml).

## Learning Goals

- Understand the complexity trade-offs in different architectures
- See patterns pushed to their extremes
- Learn when simpler is better
- Pick useful concepts that fit your actual needs

## What You Can Learn

- State management patterns
- Testability strategies
- Dependency injection approaches
- And much more...

## What NOT to Learn

- Don't treat complexity as a badge of honor
- Don't over-engineer your production apps
- Don't cargo-cult patterns from big tech without understanding context
- Don't sacrifice maintainability for "purity"

## Contributing

Feel free to open issues or PRs if you want to:
- Add new architectural patterns
- Improve existing implementations
- Fix bugs or improve documentation

### Setup

1. Create an account on [newsapi.org](https://newsapi.org/)
2. Get your API key from the dashboard
3. Add your API key to the `local.properties` file in the project root:

```properties
NEWS_API_KEY=your_api_key_here
```

### Opening Projects in Android Studio

Use the `studiow` script to quickly open subprojects in Android Studio:

```bash
# Open the MVVM project
./studiow mvvm

# Open the state-machine (event-driven) project
./studiow state-machine

# Open the entire workspace
./studiow all

# Clean build caches before opening
./studiow --clean mvvm

# Clean build caches before opening state-machine
./studiow --clean state-machine

# List all available projects
./studiow --list

# Show help
./studiow --help
```

You can also open any project by its path: `./studiow ./path/to/project`

### Adding a New Architecture Sample

Use the sample scaffold script:

```bash
scripts/new-sample.sh redux dev.yveskalume.newsappp.redux
./studiow redux
```

The generated project is an independent Gradle root that includes `../build-logic` for shared conventions and `../shared` for the shared data/UI modules. New sample app modules should apply `alias(libs.plugins.newsapp.android.sample.app)` and keep only project-specific identity or dependencies in their own `build.gradle.kts`.

## License

This project is open source and available for educational purposes.
