# Functional programming (Arrow)

[Arrow](https://arrow-kt.io) (`arrow-core`, `arrow-optics` + KSP plugin, via `arrow-stack` BOM) is the reference
functional library. It is KMP-compatible.

## Error handling
- Fallible operations return **`Either<Error, Value>`**; do not throw across layer boundaries.
- Errors are **typed sealed hierarchies** in `domain/error` (`FalesiaError`, `SyncError`). Add a new sealed type
  per bounded context rather than reusing `Throwable`.
- At boundaries with throwing APIs (Room, ksoup, Ktor) use `Either.catch { … }.mapLeft { DomainError(it) }`.
- Compose fallible steps with the `either { }` builder / `Raise` DSL and `.bind()`; use `raise(...)` and
  `ensure`/`ensureNotNull` instead of `if/throw`.
- Consume results with `fold`, `map`, `mapLeft`, `getOrElse`; avoid `isLeft()` + casts where possible.
- Never catch `CancellationException` silently (`Either.catch` already rethrows it).

## Immutability & optics
- Domain models and UI states are immutable `data class`es.
- Domain models use `@optics` (requires an empty `companion object`) to generate lenses; use them for nested
  updates (`Falesia.nome.modify(falesia) { … }`) instead of chains of `copy`.
- Mappers are pure top-level/extension functions (`toDomain()`, `toEntity()`).

## Coroutines & concurrency
- Prefer coroutine-native constructs: `chunked(n)` + `async/awaitAll`, `Flow.flatMapMerge(concurrency = n)`.
- Avoid `Semaphore`, `Mutex` and other low-level primitives unless strictly necessary.
- Inject dispatchers (`@IoDispatcher`) instead of hardcoding `Dispatchers.IO`, so tests can replace them.
