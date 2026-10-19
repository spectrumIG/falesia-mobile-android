package it.softsamurai.sync

import arrow.core.Either
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.usecase.DbFreshnessStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncWorkOutcomeTest {

    private var syncCalls = 0
    private val failure = SyncError.NetworkFailure(RuntimeException("offline"))

    private fun syncReturning(result: Either<SyncError, Unit>): suspend () -> Either<SyncError, Unit> = {
        syncCalls++
        result
    }

    @Test
    fun `fresh data succeeds without syncing`() = runTest {
        val outcome = syncWorkOutcome(DbFreshnessStatus.Fresh, 0, syncReturning(Either.Right(Unit)))

        assertEquals(SyncOutcome.Success, outcome)
        assertEquals(0, syncCalls)
    }

    @Test
    fun `stale data is synced and succeeds`() = runTest {
        val outcome = syncWorkOutcome(DbFreshnessStatus.NeedsSync, 0, syncReturning(Either.Right(Unit)))

        assertEquals(SyncOutcome.Success, outcome)
        assertEquals(1, syncCalls)
    }

    @Test
    fun `failed sync is retried while attempts are below the limit`() = runTest {
        (0 until MAX_RETRIES).forEach { attempt ->
            assertEquals(
                "attempt $attempt",
                SyncOutcome.Retry,
                syncWorkOutcome(DbFreshnessStatus.NeedsSync, attempt, syncReturning(Either.Left(failure))),
            )
        }
    }

    @Test
    fun `failed sync gives up once the retry limit is reached`() = runTest {
        val outcome = syncWorkOutcome(DbFreshnessStatus.NeedsSync, MAX_RETRIES, syncReturning(Either.Left(failure)))

        assertEquals(SyncOutcome.Failure, outcome)
    }
}
