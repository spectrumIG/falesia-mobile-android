package it.softsamurai.domain.time

/**
 * Source of the current time, injected so time-based rules (data freshness, sync timestamps) are
 * deterministic in tests. Plain Kotlin, no platform API: ready for KMP.
 */
fun interface TimeProvider {
    /** Milliseconds since the Unix epoch. */
    fun nowMillis(): Long
}
