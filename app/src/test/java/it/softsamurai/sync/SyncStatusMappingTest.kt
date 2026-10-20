package it.softsamurai.sync

import androidx.work.WorkInfo
import it.softsamurai.domain.sync.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncStatusMappingTest {

    @Test
    fun `running work is Running`() {
        assertEquals(SyncStatus.Running, syncStatusOf(WorkInfo.State.RUNNING, runAttemptCount = 0))
    }

    @Test
    fun `work waiting before its first attempt is Pending`() {
        assertEquals(SyncStatus.Pending, syncStatusOf(WorkInfo.State.ENQUEUED, runAttemptCount = 0))
        assertEquals(SyncStatus.Pending, syncStatusOf(WorkInfo.State.BLOCKED, runAttemptCount = 0))
    }

    @Test
    fun `work waiting for a retry after an attempt is Failed`() {
        assertEquals(SyncStatus.Failed, syncStatusOf(WorkInfo.State.ENQUEUED, runAttemptCount = 1))
    }

    @Test
    fun `failed or cancelled work is Failed, succeeded work is Idle`() {
        assertEquals(SyncStatus.Failed, syncStatusOf(WorkInfo.State.FAILED, runAttemptCount = 3))
        assertEquals(SyncStatus.Failed, syncStatusOf(WorkInfo.State.CANCELLED, runAttemptCount = 0))
        assertEquals(SyncStatus.Idle, syncStatusOf(WorkInfo.State.SUCCEEDED, runAttemptCount = 1))
    }
}
