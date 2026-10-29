package it.softsamurai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDetailTest {

    private val crag = Falesia(
        id = "arco",
        nome = "Arco",
        zona = "",
        pictureUrl = null,
        latitude = null,
        longitude = null,
        numeroVie = 4,
        linkDettagli = "",
        lastUpdated = 0L,
        vie = listOf(
            Via(id = 30, nome = "A", grado = "6a", settoreId = 1),
            Via(id = 10, nome = "B", grado = "6b", settoreId = 1),
            Via(id = 20, nome = "C", grado = "6c", settoreId = 2),
            Via(id = 5, nome = "D", grado = "7a", settoreId = 1),
        ),
    )

    @Test
    fun `routes of a sector keep the site's order`() {
        assertEquals(listOf(30L, 10L, 5L), crag.vieOfSettore(1).map { it.id })
        assertEquals(listOf(20L), crag.vieOfSettore(2).map { it.id })
        assertTrue(crag.vieOfSettore(3).isEmpty())
    }

    @Test
    fun `route numbers follow the order of the list from 1`() {
        assertEquals(mapOf(30L to 1, 10L to 2, 5L to 3), crag.vieOfSettore(1).routeNumbers())
        assertTrue(emptyList<Via>().routeNumbers().isEmpty())
    }

    @Test
    fun `topo finds the line of a route`() {
        val topo = Topo(
            settoreId = 1,
            imageUrl = null,
            frame = TopoRect(0.0, 0.0, 10.0, 10.0),
            image = TopoRect(0.0, 0.0, 10.0, 10.0),
            lines = listOf(TopoLine(10, listOf("M0,0L1,1"))),
        )

        assertEquals(listOf("M0,0L1,1"), topo.lineOf(10)?.segments)
        assertNull(topo.lineOf(30))
    }
}
