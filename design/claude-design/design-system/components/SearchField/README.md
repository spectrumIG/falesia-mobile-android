# SearchField

Search input with a leading search icon, in two sizes: the large field in Discover's header and the compact one in the route list.

- Provide: a `placeholder` that says what is searched ("Search climbing areas...", "Search routes by name or grade...") and a visually hidden `<label>`.
- Large: `size-field` tall, `field` fill, `radius-xl`, 2px `primary` border on focus, text in `input`.
- Compact: `primary-5` fill, `primary-60` icon, `body-sm` text, `focus-ring` on focus.
- The placeholder colors are below 4.5:1 (see `field-placeholder`); never rely on the placeholder as the only label.
