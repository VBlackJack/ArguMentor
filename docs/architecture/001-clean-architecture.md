# ADR 001: Clean Architecture with MVVM

## Status
Accepted

## Date
2024-01-01

## Context

ArguMentor is an Android application for argumentation and critical thinking. We needed to choose an architecture that would:
- Scale well as features are added
- Be testable at all layers
- Follow Android best practices
- Support offline-first functionality
- Allow for clear separation of concerns

## Decision

We adopt **Clean Architecture** with **MVVM** (Model-View-ViewModel) pattern using the following technology stack:
- **UI Layer**: Jetpack Compose with Material 3
- **ViewModel Layer**: Android Architecture Components ViewModel with StateFlow
- **Domain Layer**: Kotlin data classes and repository interfaces
- **Data Layer**: Room Database with DAOs

### Layer Structure

```
app/
├── data/
│   ├── local/           # Room database, DAOs, migrations
│   ├── model/           # Entity classes (@Entity, data classes)
│   ├── repository/      # Repository implementations
│   ├── dto/             # Data Transfer Objects for import/export
│   └── export/          # PDF and Markdown exporters
├── di/                  # Hilt dependency injection modules
├── ui/
│   ├── components/      # Reusable Compose components
│   ├── navigation/      # Navigation graph
│   ├── screens/         # Feature screens with ViewModels
│   └── theme/           # Material 3 theming
└── util/                # Utilities, helpers, extensions
```

### Data Flow

```
UI (Compose) → ViewModel → Repository → DAO → Room Database
     ↑            ↓
     └─── StateFlow/Flow ───┘
```

## Rationale

### Why Clean Architecture?
1. **Testability**: Each layer can be unit tested independently
2. **Maintainability**: Changes in one layer don't affect others
3. **Scalability**: New features follow established patterns
4. **Dependency Rule**: Dependencies point inward (UI → Domain → Data)

### Why MVVM over MVI?
1. **Simplicity**: MVVM is simpler for CRUD-heavy applications
2. **Android Support**: First-class support in Android Architecture Components
3. **StateFlow**: Provides reactive state management without MVI complexity
4. **Familiar Pattern**: Most Android developers know MVVM

### Why Hilt for DI?
1. **Compile-time Safety**: Errors caught at build time
2. **Android Integration**: Built for Android lifecycle
3. **Less Boilerplate**: Compared to Dagger 2
4. **Testing Support**: Easy to swap dependencies in tests

## Consequences

### Positive
- Clear separation of concerns
- Highly testable codebase
- Easy to onboard new developers
- Consistent patterns across features

### Negative
- More boilerplate than simpler architectures
- Learning curve for Clean Architecture concepts
- Potential over-engineering for simple features

### Mitigation
- Use IDE templates for new features
- Document patterns in this ADR
- Allow pragmatic shortcuts for trivial features

## Implementation Notes

### ViewModels
- Use `@HiltViewModel` annotation
- Expose state via `StateFlow` (not `LiveData`)
- Use `SharingStarted.WhileSubscribed(5000)` for lifecycle awareness
- Handle errors with `UiState` sealed class

### Repositories
- Single source of truth for data
- Return `Flow<T>` for reactive data
- Return `Result<T>` for one-shot operations
- Handle caching and sync logic

### DAOs
- Use `@Query` with raw SQL for complex queries
- Return `Flow<List<T>>` for observable queries
- Use `suspend fun` for write operations

## References
- [Guide to app architecture - Android Developers](https://developer.android.com/topic/architecture)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
