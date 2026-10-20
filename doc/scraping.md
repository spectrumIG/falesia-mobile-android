# Scraping falesiaonline.it

## Pages and what they give

| Page                                                        | When                      | Parser                 | Data                                                                                                                                                                                                                                                                                                                           |
|-------------------------------------------------------------|---------------------------|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Crag list (POST `/falesie/sud-europa.html`)                 | weekly sync               | `parseElencoFalesie`   | `li.leafletshadow`: name, `/falesia/<slug>.html` link, route count                                                                                                                                                                                                                                                             |
| Crag page `/falesia/<slug>.html`                            | weekly sync, one per crag | `parseDettagliFalesia` | `h1` (required), `#bread_crumb li` → zone, `header.pageHeader div.stars` → rating and votes, `.rock_icon`, `.season_icon`, `article.narrow p` → description, `#objectPhoto img`, `li.sectorleaflet` → sectors, `L.marker([lat,lng], …)` + `sector_topo_sketch_or_photo/<id>` → sector positions (crag position = their centre) |
| Sector page `/settorefoto/<id>/…`, `/settoredisegno/<id>/…` | on demand (crag details)  | `parseVieSettore`      | `#routes_list` (required) `div.routelistinnerelement` (id `routeslist_element_<id>`): name, stars, votes, grade, protection icon, length                                                                                                                                                                                       |
| Map page `/falesia/map/<slug>.html`                         | on demand (crag details)  | `parseParcheggio`      | `parkingLat = …, parkingLong = …` in the map script (optional)                                                                                                                                                                                                                                                                 |

- **There are no routes on the crag page**: they are on the sector pages, so they are downloaded
  when the crag is opened, not in the weekly sync (one request per sector, see
  [offline-first-and-sync.md](offline-first-and-sync.md)).
- Sector indicators: `span.indicator[title=Vie|Sviluppo delle vie|Avvicinamento|Esposizione]`; the
  grade range is the `span.indicator` without title (`6a+<div title="Grado medio 7a"/>7c+`).
- Stars widgets: count `star_full` (1) and `star_half` (0.5) images; the text is the number of votes
  (no votes → no rating). A sector page repeats every route's stars in a detail pane after the list:
  read the stars inside
  `.starsandroutename` only.
- Exposures are Italian abbreviations (`SO` = south-west, `O` = west), mapped by `parseExposures`.
- Protection icon `bolted_*` → `ViaStyle.Sport`; `trad*`/`clean*`/`natural*` → `Trad`; `mixed*`/
  `misto*` → `Mixed`. Only `bolted` was seen on the saved pages.
- Grades are French and kept as text; `domain/model/FrenchGrade` orders them.

## Where

- Interface: `repository/network/FalesiaNetworkDataSource.kt` (`fetchElencoFalesie()`,
  `fetchDettagliFalesia(url)`,
  `fetchVieSettore(url)`, `fetchParcheggio(detailUrl)`).
- Implementation: `FalesiaNetDataSourceImpl` in the same file, using **ksoup** + **ksoup-network** (Ktor client
  underneath; Ktor request builders like `headers {}`/`setBody()` are used). It only performs the HTTP calls.
- Parsing: pure functions in `repository/network/FalesiaHtmlParser.kt` (`parseElencoFalesie`,
  `parseDettagliFalesia`, `parseVieSettore`, `parseParcheggio`, `parseExposures`,
  `Document.extractCoordinates`,
  `resolveImageUrl`). All selectors live there.
- Network models: `repository/network/model/`. Mapping to entities/domain: `repository/mapper/FalesiaMapper.kt`.
- Base URL is provided by Hilt in `repository/di/DataModule.kt`.

## Rules
- All HTML/CSS-selector knowledge stays **inside the network data source**. Nothing outside the data layer knows
  about HTML.
- Every function returns `Either<SyncError, …>`; wrap ksoup calls with `Either.catch { }.mapLeft { }`.
  Use `SyncError.NetworkFailure` for transport problems and `SyncError.ParseFailure` for unexpected
  markup (a crag page without `h1`, a sector page without `#routes_list`).
- Parse defensively: missing elements must produce sensible defaults or a `ParseFailure`, never a crash.
- Selectors are fragile (the site can change anytime): cover parsing with **unit tests on saved HTML fixtures**
  (`app/src/test/resources/AllCragsResponse.html`, `SingleCragPage.html` (Monsummano),
  `SectorPage.html`,
  `CragMapPage.html`; see `FalesiaHtmlParserTest`), plus URL helpers (`ResolveImageUrlTest`). The
  last three were saved from the Wayback Machine (2025–2026 copies) on 2026-10-20 while the site was
  down: see known issues.
- Be a polite client of a free public site: bounded concurrency (`SYNC_CONCURRENCY`), keep the `Referer` header,
  sync at most on demand / weekly, never add aggressive polling.
- ksoup is KMP-compatible: do not introduce Jsoup or other JVM-only parsers.
- ksoup does not fail on HTTP errors: an error page ("502 Bad Gateway") is parsed like any page. A
  list page without crags is therefore a `ParseFailure` (`parseElencoFalesieOrFail`), never an empty
  successful sync.
- Regexes run on Android's **ICU** engine, stricter than the JVM one used by unit tests: escape
  literal
  `{ } [ ]` (a bare `}` made the parser class fail to load on devices).
  `FalesiaHtmlParserDeviceTest`
  (instrumented) exercises every pattern on a device.
