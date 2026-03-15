package it.softsamurai.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllFalesieUseCaseTest {

    private lateinit var repository: FalesiaRepository
    private lateinit var useCase: GetAllFalesieUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllFalesieUseCase(repository)
    }

    @Test
    fun `emits list of falesie from repository`() = runTest {
        val falesie = listOf(fakeFalesia("1"), fakeFalesia("2"))
        every { repository.observeAllFalesie() } returns flowOf(falesie)

        useCase().test {
            assertEquals(falesie, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `emits empty list when repository has no data`() = runTest {
        every { repository.observeAllFalesie() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Falesia>(), awaitItem())
            awaitComplete()
        }
    }

    private fun fakeFalesia(id: String) = Falesia(
        id = id,
        nome = "Falesia $id",
        zona = "",
        pictureUrl = null,
        latitude = null,
        longitude = null,
        numeroVie = 10,
        linkDettagli = "https://example.com/$id",
        vie = emptyList(),
        lastUpdated = 0L,
    )
}
