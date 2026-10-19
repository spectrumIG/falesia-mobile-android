package it.softsamurai.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CheckDbFreshnessUseCaseTest {

    private val now = 1_700_000_000_000L
    private lateinit var repository: FalesiaRepository
    private lateinit var useCase: CheckDbFreshnessUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CheckDbFreshnessUseCase(repository) { now }
    }

    private fun givenDb(count: Int, oldest: Long?) {
        coEvery { repository.countFalesie() } returns count
        coEvery { repository.getOldestUpdateTimestamp() } returns oldest
    }

    @Test
    fun `returns NeedsSync when DB is empty without reading timestamps`() = runTest {
        givenDb(count = 0, oldest = now)

        assertEquals(DbFreshnessStatus.NeedsSync, useCase())
        coVerify(exactly = 0) { repository.getOldestUpdateTimestamp() }
    }

    @Test
    fun `returns NeedsSync when oldest timestamp is null`() = runTest {
        givenDb(count = 5, oldest = null)

        assertEquals(DbFreshnessStatus.NeedsSync, useCase())
    }

    @Test
    fun `returns NeedsSync when data is older than one week`() = runTest {
        givenDb(count = 10, oldest = now - 8L * 24 * 60 * 60 * 1000)

        assertEquals(DbFreshnessStatus.NeedsSync, useCase())
    }

    @Test
    fun `returns Fresh when data is less than one week old`() = runTest {
        givenDb(count = 10, oldest = now - 3L * 24 * 60 * 60 * 1000)

        assertEquals(DbFreshnessStatus.Fresh, useCase())
    }

    @Test
    fun `data exactly one week old is still Fresh`() = runTest {
        givenDb(count = 1, oldest = now - ONE_WEEK_MILLIS)

        assertEquals(DbFreshnessStatus.Fresh, useCase())
    }

    @Test
    fun `data one millisecond past one week NeedsSync`() = runTest {
        givenDb(count = 1, oldest = now - ONE_WEEK_MILLIS - 1)

        assertEquals(DbFreshnessStatus.NeedsSync, useCase())
    }
}
