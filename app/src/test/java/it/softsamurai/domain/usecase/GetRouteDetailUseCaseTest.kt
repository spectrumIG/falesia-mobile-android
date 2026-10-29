package it.softsamurai.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Topo
import it.softsamurai.domain.model.TopoRect
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.domain.repository.FalesiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetRouteDetailUseCaseTest {

    private val repository = mockk<FalesiaRepository>()
    private val useCase = GetRouteDetailUseCase(repository)

    @Test
    fun `combines the route, its sector, the sector's routes and the sector's topo`() = runTest {
        every { repository.observeFalesiaWithVie("arco") } returns flowOf(crag)
        every { repository.observeTopo(1) } returns flowOf(topo(1))

        useCase("arco", 11).test {
            val detail = awaitItem()!!
            assertEquals(11L, detail.via.id)
            assertEquals("Placche", detail.settore?.nome)
            assertEquals(listOf(10L, 11L), detail.sectorVie.map { it.id })
            assertEquals(1L, detail.topo?.settoreId)
            awaitComplete()
        }
    }

    @Test
    fun `a sector without topo still gives the route`() = runTest {
        every { repository.observeFalesiaWithVie("arco") } returns flowOf(crag)
        every { repository.observeTopo(2) } returns flowOf(null)

        useCase("arco", 20).test {
            val detail = awaitItem()!!
            assertNull(detail.topo)
            assertEquals(listOf(20L), detail.sectorVie.map { it.id })
            awaitComplete()
        }
    }

    @Test
    fun `missing crag or route emits null without reading a topo`() = runTest {
        every { repository.observeFalesiaWithVie("arco") } returns flowOf(crag)
        every { repository.observeFalesiaWithVie("gone") } returns flowOf(null)

        useCase("arco", 99).test {
            assertNull(awaitItem())
            awaitComplete()
        }
        useCase("gone", 10).test {
            assertNull(awaitItem())
            awaitComplete()
        }
        verify(exactly = 0) { repository.observeTopo(any()) }
    }

    @Test
    fun `new routes from the DB update the detail`() = runTest {
        val crags = MutableStateFlow(crag)
        every { repository.observeFalesiaWithVie("arco") } returns crags
        every { repository.observeTopo(1) } returns flowOf(topo(1))

        useCase("arco", 10).test {
            assertEquals("Uno", awaitItem()!!.via.nome)
            crags.value =
                crag.copy(vie = crag.vie.map { if (it.id == 10L) it.copy(nome = "Uno bis") else it })
            assertEquals("Uno bis", awaitItem()!!.via.nome)
        }
    }

    private fun topo(settoreId: Long) = Topo(
        settoreId = settoreId,
        imageUrl = null,
        frame = TopoRect(0.0, 0.0, 400.0, 300.0),
        image = TopoRect(0.0, 0.0, 400.0, 300.0),
        lines = emptyList(),
    )

    private fun settore(id: Long, nome: String) = Settore(
        id = id,
        nome = nome,
        numeroVie = 0,
        gradoMin = "",
        gradoMax = "",
        length = "",
        approachMinMinutes = null,
        approachMaxMinutes = null,
        exposures = emptyList(),
        rating = null,
        ratingCount = 0,
        style = ViaStyle.Sport,
        location = null,
        linkDettagli = "",
    )

    private val crag = Falesia(
        id = "arco",
        nome = "Arco",
        zona = "",
        pictureUrl = null,
        latitude = null,
        longitude = null,
        numeroVie = 3,
        linkDettagli = "",
        lastUpdated = 0L,
        settori = listOf(settore(1, "Placche"), settore(2, "Strapiombi")),
        vie = listOf(
            Via(id = 10, nome = "Uno", grado = "6a", settoreId = 1),
            Via(id = 11, nome = "Due", grado = "6b", settoreId = 1),
            Via(id = 20, nome = "Tre", grado = "7a", settoreId = 2),
        ),
    )
}
