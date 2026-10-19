# Code quality & workflow

## Before declaring a task done
1. Code compiles: `./gradlew :app:compileDebugKotlin` (or IDE diagnostics). Check imports and Gradle deps
   explicitly; reading the code is not enough.
2. Unit tests pass: `./gradlew :app:testDebugUnitTest`.
3. New/changed behavior is tested ([testing.md](testing.md)).
4. Relevant `doc/` files and [../AGENTS.md](../AGENTS.md) are updated.

## Style
- Kotlin official code style (`kotlin.code.style=official`); match surrounding code (section comments like
  `// ── Observe ──` in repositories, KDoc only where it adds information).
- Domain vocabulary is **Italian** and must be kept: `falesia`/`falesie` (crag/crags), `via`/`vie`
  (route/routes), `zona`, `nome`. Everything else (code structure, comments, docs) in English.
- `private const val` for magic numbers at file top (e.g. `SYNC_CONCURRENCY`, `MAX_RETRIES`).
- detekt with `buildUponDefaultConfig`; Android lint with `app/lint-baseline.xml` (don't grow the baseline to
  hide new issues).

## Git
- Integration branch: **`develop`**. Work on a feature branch and open PRs to `develop`, **never to `main`**.
- `main` holds released code only; it is updated from `develop` by the project owner.
- Remote name: `falesia-mobile-android`.
- Commit or push only when the user asks.

## Secrets
- `keystore.properties`, `*.jks`, `local.properties` are local/ignored: never commit, print or modify them.
