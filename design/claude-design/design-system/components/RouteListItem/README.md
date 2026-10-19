# RouteListItem

A route in a crag's route list: name with status, protection, rating and type on the left, big grade on the right.

- Provide: name, optional status (`fm-status` SENT or PROJECT), protection (`bolt` with "12 bolts", or `hardware` with "Nuts/Cams"), rating, type, grade.
- Card: `surface-card-neutral`, `primary-10` border (`primary-40` on hover), `radius-xl`, padding `space-4`.
- Name in `title-sm`; meta in `body-sm` `ink-muted` with `primary` icons; grade in `grade-xl` `primary` over a `micro` GRADE caption.
