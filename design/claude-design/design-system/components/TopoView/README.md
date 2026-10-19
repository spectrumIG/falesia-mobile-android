# TopoView

A route photo with its line drawn on top, a crux label, the grade badge and a zoom button.

- Provide: the photo (as `background-image`), the line as an SVG path in a 400×500 viewBox, crux labels with their position, the grade.
- Line: `primary`, 6 wide, round caps and joins, 90% opacity, 8-radius dots at both ends. Crux label: `overlay-strong` pill, white 12px semibold.
- Bottom bar: `fm-topo-badge` (`overlay` with blur, grade in `fm-topo-grade`, "Topo View") and a `size-icon-button-lg` `zoom_in` button in `primary`.
- 3:4, full width.
