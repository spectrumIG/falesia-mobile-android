package it.softsamurai.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CheckDbFreshnessUseCaseTest {

    private lateinit var repository: FalesiaRepository
    private lateinit var useCase: CheckDbFreshnessUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = CheckDbFreshnessUseCase(repository)
    }

    @Test
    fun `returns NeedsSync when DB is empty`() = runTest {
        coEvery { repository.countFalesie() } returns 0

        val result = useCase()

        assertEquals(DbFreshnessStatus.NeedsSync, result)
    }

    @Test
    fun `returns NeedsSync when oldest timestamp is null`() = runTest {
        coEvery { repository.countFalesie() } returns 5
        coEvery { repository.getOldestUpdateTimestamp() } returns null

        val result = useCase()

        assertEquals(DbFreshnessStatus.NeedsSync, result)
    }

    @Test
    fun `returns NeedsSync when data is older than one week`() = runTest {
        val eightDaysAgo = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000)
        coEvery { repository.countFalesie() } returns 10
        coEvery { repository.getOldestUpdateTimestamp() } returns eightDaysAgo

        val result = useCase()

        assertEquals(DbFreshnessStatus.NeedsSync, result)
    }

    @Test
    fun `returns Fresh when data is less than one week old`() = runTest {
        val threeDaysAgo = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000)
        coEvery { repository.countFalesie() } returns 10
        coEvery { repository.getOldestUpdateTimestamp() } returns threeDaysAgo

        val result = useCase()

        assertEquals(DbFreshnessStatus.Fresh, result)
    }

    @Test
    fun `returns Fresh when data is exactly at the boundary (just under one week)`() = runTest {
        val justUnderOneWeek = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000 - 1000)
        coEvery { repository.countFalesie() } returns 1
        coEvery { repository.getOldestUpdateTimestamp() } returns justUnderOneWeek

        val result = useCase()

        assertEquals(DbFreshnessStatus.Fresh, result)
    }
}
