package it.softsamurai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FalesiaStatsTest {

    private fun settore(
        id: Long,
        gradoMin: String = "",
        gradoMax: String = "",
        approach: Pair<Int?, Int?> = null to null,
        exposures: List<Exposure> = emptyList(),
        numeroVie: Int = 10,
    ) = Settore(
        id = id,
        nome = "S$id",
        numeroVie = numeroVie,
        gradoMin = gradoMin,
        gradoMax = gradoMax,
        length = "",
        approachMinMinutes = approach.first,
        approachMaxMinutes = approach.second,
        exposures = exposures,
        rating = null,
        ratingCount = 0,
        style = ViaStyle.Sport,
        location = null,
        linkDettagli = "",
    )

    private fun falesia(
        settori: List<Settore> = emptyList(),
        vie: List<Via> = emptyList(),
        latitude: Double? = null,
        longitude: Double? = null,
        parking: GeoPoint? = null,
    ) = Falesia(
        id = "x",
        nome = "X",
        zona = "",
        pictureUrl = null,
        latitude = latitude,
        longitude = longitude,
        numeroVie = 0,
        linkDettagli = "",
        vie = vie,
        lastUpdated = 0L,
        settori = settori,
        parking = parking,
    )

    // ── gradeRange ──

    @Test
    fun `grade range spans the easiest and hardest sector grade`() {
        val range = falesia(
            settori = listOf(settore(1, "6a+", "7c+"), settore(2, "4a", "8b"), settore(3)),
        ).gradeRange()!!

        assertEquals("4a", range.start.toString())
        assertEquals("8b", range.endInclusive.toString())
    }

    @Test
    fun `grade range falls back to the routes`() {
        val range = falesia(
            vie = listOf(
                Via(nome = "a", grado = "6b"),
                Via(nome = "b", grado = "5c")
            )
        ).gradeRange()!!

        assertEquals("5c", range.start.toString())
        assertEquals("6b", range.endInclusive.toString())
    }

    @Test
    fun `no grades means no range`() {
        assertNull(falesia(vie = listOf(Via(nome = "a", grado = "?"))).gradeRange())
    }

    // ── approachRange ──

    @Test
    fun `approach range spans all sectors and handles single values`() {
        val range = falesia(
            settori = listOf(
                settore(1, approach = 6 to 15),
                settore(2, approach = 3 to 5),
                settore(3, approach = 30 to null)
            ),
        ).approachRange()

        assertEquals(3..30, range)
    }

    @Test
    fun `no approach data means no range`() {
        assertNull(falesia(settori = listOf(settore(1))).approachRange())
    }

    // ── mainExposures ──

    @Test
    fun `main exposures are weighted by the number of routes`() {
        val exposures = falesia(
            settori = listOf(
                settore(1, exposures = listOf(Exposure.NE), numeroVie = 5),
                settore(2, exposures = listOf(Exposure.SW, Exposure.S), numeroVie = 20),
                settore(3, exposures = listOf(Exposure.SW), numeroVie = 10),
                settore(4, exposures = listOf(Exposure.E), numeroVie = 1),
            ),
        ).mainExposures()

        assertEquals(listOf(Exposure.SW, Exposure.S, Exposure.NE), exposures)
    }

    // ── featuredVie ──

    @Test
    fun `featured routes are the best rated, then the most voted, routes without votes last`() {
        val vie = listOf(
            Via(id = 1, nome = "none", grado = "6a"),
            Via(id = 2, nome = "good", grado = "6a", rating = 4.0, ratingCount = 2),
            Via(id = 3, nome = "best", grado = "6a", rating = 5.0, ratingCount = 1),
            Via(id = 4, nome = "good, more votes", grado = "6a", rating = 4.0, ratingCount = 9),
        )

        assertEquals(listOf(3L, 4L, 2L), falesia(vie = vie).featuredVie().map { it.id })
    }

    // ── navigationTarget ──

    @Test
    fun `navigation goes to the parking, else to the crag, else nowhere`() {
        val parking = GeoPoint(1.0, 2.0)

        assertEquals(
            parking,
            falesia(latitude = 3.0, longitude = 4.0, parking = parking).navigationTarget()
        )
        assertEquals(
            GeoPoint(3.0, 4.0),
            falesia(latitude = 3.0, longitude = 4.0).navigationTarget()
        )
        assertNull(falesia(latitude = 3.0).navigationTarget())
    }
}
