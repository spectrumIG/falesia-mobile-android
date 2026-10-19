package it.softsamurai.core.ui.component

import androidx.annotation.DrawableRes
import it.softsamurai.core.ui.R

/** Leading action of a [FalesiaTopAppBar]: `menu` on top-level screens, `arrow_back` on detail screens. */
sealed interface TopBarNavigation {
    val contentDescription: String
    val onClick: () -> Unit

    @get:DrawableRes
    val icon: Int

    data class Menu(
        override val contentDescription: String,
        override val onClick: () -> Unit = {}
    ) :
        TopBarNavigation {
        override val icon: Int get() = R.drawable.ic_menu
    }

    data class Back(override val contentDescription: String, override val onClick: () -> Unit) :
        TopBarNavigation {
        override val icon: Int get() = R.drawable.ic_arrow_back
    }
}
