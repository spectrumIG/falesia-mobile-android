# CragCard

A crag in the Discover list: 16:9 photo, name, location, style tag, two stats and a round chevron button.

- Provide: photo, name, location (region, country), style (Sport, Bouldering, Trad), two stats as label and value (Routes plus Best Grade, Distance or Style).
- Card: `surface-card`, `line` border, `radius-xl`, `shadow-sm`; body padding `space-5`; stats above a `line-divider`.
- Name in `card-title`, location in `body-sm` `ink-muted` with `location_on`, stat labels in `label`, values in `stat`.
- The whole card opens the crag; the chevron button is its visible affordance (`size-icon-button`, `primary` fill).
