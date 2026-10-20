package it.softsamurai.repository.network

import com.fleeksoft.ksoup.Ksoup
import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.FrenchGrade
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The parser's regexes on Android's ICU engine, which rejects some patterns the JVM accepts (a bare `}` made
 * the whole parser fail to load on devices while JVM unit tests passed). Every pattern is exercised here;
 * the full parsing is covered by `FalesiaHtmlParserTest` on saved pages.
 */
class FalesiaHtmlParserDeviceTest {

    private fun html(body: String) = Ksoup.parse("<html><body>$body</body></html>")

    @Test
    fun cragPage_sectorPositionComesFromItsMarker() {
        val detail = parseDettagliFalesia(
            html(
                """<h1>Falesia: X</h1><ul><li class="leafletshadow sectorleaflet">
                <h4><a href="/settorefoto/242/cava.html">Cava</a></h4>
                <div class="indicatorrow otherrows"><span class="indicator" title="Avvicinamento">3–8min</span></div>
                </li></ul>
                <script>sectorMarker = L.marker([43.86, 10.83], { icon: divIcon, title: 'Cava' })
                .addTo(map)
                .on('click', function () { location.href='/sector_topo_sketch_or_photo/242'; });</script>""",
            ),
        ).getOrNull()!!

        val settore = detail.settori.single()
        assertEquals(242L, settore.id)
        assertEquals(43.86, settore.latitude!!, 1e-9)
        assertEquals(3, settore.approachMinMinutes)
        assertEquals(8, settore.approachMaxMinutes)
    }

    @Test
    fun coordinatesFallBackToSetViewAndMarker() {
        assertEquals(
            45.9 to 10.8,
            html("<script>map.setView([45.9, 10.8], 13);</script>").extractCoordinates()
        )
        assertEquals(
            44.1 to 8.3,
            html("<script>L.marker([44.1, 8.3]);</script>").extractCoordinates()
        )
    }

    @Test
    fun sectorPage_routeGradeAndLength() {
        val via = parseVieSettore(
            html(
                """<div id="routes_list"><div id="routeslist_element_7" class="routelistinnerelement">
                <div class="starsandroutename">Spigolo</div><div class="routedetails">6b+ &#160; 15–20m</div>
                </div></div>""",
            ),
        ).getOrNull()!!.single()

        assertEquals("6b+", via.grado)
        assertEquals("15–20m", via.length)
    }

    @Test
    fun mapPage_parking() {
        assertEquals(
            42.88 to 11.57,
            parseParcheggio(html("<script>var parkingLat = 42.88, parkingLong = 11.57;</script>")),
        )
    }

    @Test
    fun exposuresAndGrades() {
        assertEquals(listOf(Exposure.SE, Exposure.S, Exposure.SW), parseExposures("SE-S-SO"))
        assertEquals("7a+", FrenchGrade.parse("7a+/7b").toString())
    }
}
