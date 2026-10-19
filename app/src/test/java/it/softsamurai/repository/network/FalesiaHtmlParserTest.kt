package it.softsamurai.repository.network

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FalesiaHtmlParserTest {

    private fun fixture(name: String): Document =
        Ksoup.parse(requireNotNull(javaClass.classLoader?.getResource(name)) { "Missing fixture $name" }.readText())

    private fun html(body: String): Document = Ksoup.parse("<html><body>$body</body></html>")

    // ── Crag list (saved POST response) ──

    @Test
    fun `list page yields every crag`() {
        val crags = parseElencoFalesie(fixture("AllCragsResponse.html"))

        assertEquals(90, crags.size)
    }

    @Test
    fun `list item has name, route count and absolute detail url`() {
        val first = parseElencoFalesie(fixture("AllCragsResponse.html")).first()

        assertEquals("Monsummano", first.nome)
        assertEquals(231, first.routeCount)
        assertEquals("https://www.falesiaonline.it/falesia/monsummano.html", first.detailUrl)
        assertNull(first.pictureUrl)
    }

    @Test
    fun `every list item links to a falesiaonline detail page`() {
        val crags = parseElencoFalesie(fixture("AllCragsResponse.html"))

        assertTrue(crags.all { it.detailUrl.startsWith("https://www.falesiaonline.it/falesia/") })
        assertTrue(crags.all { it.nome.isNotBlank() && it.nome != "N/A" })
    }

    @Test
    fun `list item without link or route counter falls back to defaults`() {
        val crags = parseElencoFalesie(
            html(
                """<ul><li class="leafletshadow"><h4>Senza link</h4>
                <div class="indicatorrow firstrow"><span class="indicator" title="Roccia">Calcare</span></div>
                </li></ul>""",
            ),
        )

        assertEquals(1, crags.size)
        assertEquals("N/A", crags[0].nome)
        assertEquals(0, crags[0].routeCount)
        assertEquals("https://www.falesiaonline.it", crags[0].detailUrl)
    }

    @Test
    fun `non numeric route counter becomes zero`() {
        val crags = parseElencoFalesie(
            html(
                """<ul><li class="leafletshadow"><h4><a href="/falesia/x.html">X</a></h4>
                <div class="indicatorrow firstrow"><span class="indicator" title="Vie">n.d.</span></div>
                </li></ul>""",
            ),
        )

        assertEquals(0, crags.single().routeCount)
    }

    // ── Crag detail ──

    @Test
    fun `detail page photo is resolved to an absolute url`() {
        val detail = parseDettagliFalesia(fixture("SingleCragPage.html"))

        assertEquals(
            "https://www.falesiaonline.it/uploads/assets/image/150224245784falesia_monsummano_.jpg",
            detail.pictureUrl,
        )
    }

    @Test
    fun `routes are read from route-details blocks`() {
        val detail = parseDettagliFalesia(
            html(
                """<div class="route-details"><span class="nome-via">Via del Sole</span>
                <div class="route-grade">6a+</div></div>
                <div class="route-details"><span class="nome-via">Placca</span></div>""",
            ),
        )

        assertEquals(2, detail.vie.size)
        assertEquals("Via del Sole", detail.vie[0].nome)
        assertEquals("6a+", detail.vie[0].grado)
        assertEquals("", detail.vie[1].grado)
    }

    @Test
    fun `detail without photo, routes or coordinates has empty values`() {
        val detail = parseDettagliFalesia(html("<p>nothing here</p>"))

        assertNull(detail.pictureUrl)
        assertNull(detail.latitude)
        assertNull(detail.longitude)
        assertTrue(detail.vie.isEmpty())
    }

    // ── Coordinates ──

    @Test
    fun `coordinates from data attributes`() {
        val coords = html("""<div id="map" data-lat="43.87" data-lng="10.82"></div>""").extractCoordinates()

        assertEquals(43.87 to 10.82, coords)
    }

    @Test
    fun `coordinates from setView`() {
        val coords = html("<script>map.setView([ 45.92 , 10.88 ], 13);</script>").extractCoordinates()

        assertEquals(45.92 to 10.88, coords)
    }

    @Test
    fun `coordinates from L marker with negative values`() {
        val coords = html("<script>L.marker([-33.5, -70.25]).addTo(map);</script>").extractCoordinates()

        assertEquals(-33.5 to -70.25, coords)
    }

    @Test
    fun `data attributes win over scripts`() {
        val coords = html(
            """<div data-lat="1.5" data-lng="2.5"></div><script>map.setView([9.0, 9.0], 13);</script>""",
        ).extractCoordinates()

        assertEquals(1.5 to 2.5, coords)
    }

    @Test
    fun `invalid data attributes fall back to scripts`() {
        val coords = html(
            """<div data-lat="abc" data-lng=""></div><script>L.marker([44.1, 8.3]);</script>""",
        ).extractCoordinates()

        assertEquals(44.1 to 8.3, coords)
    }

    @Test
    fun `no coordinates returns null`() {
        assertNull(html("<script>var x = 1;</script>").extractCoordinates())
    }
}
