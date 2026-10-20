package it.softsamurai.domain.sync

/**
 * How the app talks to falesiaonline.it.
 *
 * - [Automatic] (release): startup and weekly sync, routes downloaded when a crag is opened.
 * - [Manual] (debug): nothing is downloaded on its own; the app starts on fixed sample data and only the
 *   download button in the top bar hits the site. Debug builds are run and restarted all day: automatic
 *   downloads would hammer a free public site.
 */
enum class DownloadMode { Automatic, Manual }

fun interface DownloadModeProvider {
    fun current(): DownloadMode
}
