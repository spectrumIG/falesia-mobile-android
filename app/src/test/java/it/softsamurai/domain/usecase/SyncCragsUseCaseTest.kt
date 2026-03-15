package it.softsamurai.domain.usecase

import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncCragsUseCaseTest {

    private lateinit var repository: FalesiaRepository
    private lateinit var useCase: SyncCragsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SyncCragsUseCase(repository)
    }

    @Test
    fun `returns Right Unit on successful sync`() = runTest {
        coEvery { repository.syncFalesie() } returns Either.Right(Unit)

        val result = useCase()

        assertTrue(result.isRight())
        coVerify(exactly = 1) { repository.syncFalesie() }
    }

    @Test
    fun `propagates network failure from repository`() = runTest {
        val error = SyncError.NetworkFailure(RuntimeException("timeout"))
        coEvery { repository.syncFalesie() } returns Either.Left(error)

        val result = useCase()

        assertTrue(result.isLeft())
        assertEquals(error, (result as Either.Left).value)
    }

    @Test
    fun `propagates storage failure from repository`() = runTest {
        val error = SyncError.StorageFailure(RuntimeException("db error"))
        coEvery { repository.syncFalesie() } returns Either.Left(error)

        val result = useCase()

        assertTrue(result.isLeft())
        assertEquals(error, (result as Either.Left).value)
    }
}
