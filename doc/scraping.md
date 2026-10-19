# Scraping falesiaonline.it

## Where
- Interface: `repository/network/FalesiaNetworkDataSource.kt` (`fetchElencoFalesie()`, `fetchDettagliFalesia(url)`).
- Implementation: `FalesiaNetDataSourceImpl` in the same file, using **ksoup** + **ksoup-network** (Ktor client
  underneath; Ktor request builders like `headers {}`/`setBody()` are used). It only performs the HTTP calls.
- Parsing: pure functions in `repository/network/FalesiaHtmlParser.kt` (`parseElencoFalesie`,
  `parseDettagliFalesia`, `Document.extractCoordinates`, `resolveImageUrl`). All selectors live there.
- Network models: `repository/network/model/`. Mapping to entities/domain: `repository/mapper/FalesiaMapper.kt`.
- Base URL is provided by Hilt in `repository/di/DataModule.kt`.

## Rules
- All HTML/CSS-selector knowledge stays **inside the network data source**. Nothing outside the data layer knows
  about HTML.
- Every function returns `Either<SyncError, …>`; wrap ksoup calls with `Either.catch { }.mapLeft { }`.
  Use `SyncError.NetworkFailure` for transport problems and `SyncError.ParseFailure` for unexpected markup.
- Parse defensively: missing elements must produce sensible defaults or a `ParseFailure`, never a crash.
- Selectors are fragile (the site can change anytime): cover parsing with **unit tests on saved HTML fixtures**
  (`app/src/test/resources/AllCragsResponse.html`, `SingleCragPage.html`; see `FalesiaHtmlParserTest`), plus
  URL helpers (`ResolveImageUrlTest`).
- Be a polite client of a free public site: bounded concurrency (`SYNC_CONCURRENCY`), keep the `Referer` header,
  sync at most on demand / weekly, never add aggressive polling.
- ksoup is KMP-compatible: do not introduce Jsoup or other JVM-only parsers.
