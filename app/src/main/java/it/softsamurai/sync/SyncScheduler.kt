package it.softsamurai.sync

interface SyncScheduler {
    /** Enqueues a one-time sync that runs as soon as there is network connectivity.
     *  Uses KEEP policy: if a sync is already enqueued or running, this is a no-op. */
    fun scheduleOnDemandSync()

    /** Enqueues a weekly periodic sync.
     *  Uses KEEP policy: does not reschedule if already registered. */
    fun schedulePeriodicSync()
}
