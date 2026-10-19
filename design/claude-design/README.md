# Claude Design snapshot

A copy of the app's design as it lives in Claude Design, so agents and people working on the code can read it
without access to claude.ai. **The source of truth is the Claude Design artifacts below; this folder is a
snapshot.** When the design changes there, re-export it here in the same PR as the code that follows it.

- Snapshot of: 2026-10-19
- Design system "Falesia Mobile": https://claude.ai/artifact/RAbZEfFEBx8kRkw5syHe4A
- Screens "Falesia Mobile UI": https://claude.ai/artifact/M6Zfv3snFfotKZtua44WQz
- Origin: 1:1 import of the Stitch export in [`../stitch_crag_discovery.zip`](../stitch_crag_discovery.zip).
  Open design issues are listed in [`doc/ui-compose.md`](../../doc/ui-compose.md).

## What is here

| Path | What | How to use it |
|---|---|---|
| `design-system/README.md` | Usage rules: color, type, shape, layout, imagery, icons, contrast notes | Read first |
| `design-system/tokens.json` | All tokens: colors per theme (`dark` first, `light`), type styles, spacing, radius, shadow, size | Source for `MaterialTheme` in `:core-ui` |
| `design-system/tokens.css` | The same tokens as CSS custom properties, generated from `tokens.json` | Used by the HTML files below |
| `design-system/components/<Name>/` | `README.md` (what it is, what it needs, which tokens) and `preview.html` (static markup) | One folder per component |
| `design-system/components/bundle.css` | Styles of the component previews (`fm-` classes) | Reference for paddings, sizes, states |
| `screens/*.dc.html` | One file per screen, Claude Design's source format | Read the markup for layout and exact values |
| `screens/*Light.dc.html` | Light versions: they import the dark screen with `theme="light"` | Same content, light tokens |
| `screens/canvas.json` | Canvas layout and screen titles | Map file names to screen names |
| `screens/images/` | Placeholder photos cropped from the Stitch screenshots | Not for the app |

The `.dc.html` files are not standalone web pages: they need Claude Design's runtime (`support.js`, `<x-dc>`).
Every style is inline and uses the token variables (`var(--primary)`, `var(--radius-xl)` …), so the markup is
readable as a spec.

## Screens

| File | Screen | Stitch source |
|---|---|---|
| `Main.dc.html` | Discover (crag list) | `crag_discovery_2` |
| `CragDetails.dc.html` | Crag details | `crag_details_2` |
| `RouteList.dc.html` | Route list of a crag | `route_list_2` |
| `RouteTopo.dc.html` | Route detail with topo | `route_detail_topo_2` |
| `Logbook.dc.html` | Logbook | `logbook_2` |

All screens use the app's bottom bar: Discover, Logbook, Ticklist, Profile.
