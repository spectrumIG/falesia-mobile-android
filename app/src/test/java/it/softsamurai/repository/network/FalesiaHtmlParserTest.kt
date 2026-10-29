package it.softsamurai.repository.network

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.ViaStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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
    fun `list page with crags is Right, an error page without crags is a parse failure`() {
        assertEquals(
            90,
            parseElencoFalesieOrFail(fixture("AllCragsResponse.html")).getOrNull()?.size
        )

        val badGateway = parseElencoFalesieOrFail(html("<center><h1>502 Bad Gateway</h1></center>"))

        assertTrue(badGateway.leftOrNull() is SyncError.ParseFailure)
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

    // ── Crag detail (saved page of Monsummano) ──

    private val monsummano by lazy { parseDettagliFalesia(fixture("SingleCragPage.html")).getOrNull()!! }

    @Test
    fun `detail page photo is resolved to an absolute url`() {
        assertEquals(
            "https://www.falesiaonline.it/uploads/assets/image/150224245784falesia_monsummano_.jpg",
            monsummano.pictureUrl,
        )
    }

    @Test
    fun `detail page has zone, rating, rock, seasons and description`() {
        assertEquals("Toscana Nord Ovest, Toscana", monsummano.zona)
        assertEquals(4.5, monsummano.rating)
        assertEquals(15, monsummano.ratingCount)
        assertEquals("Calcare", monsummano.rock)
        assertEquals("Inverno, Mezze stagioni", monsummano.seasons)
        assertTrue(monsummano.description.startsWith("Storica falesia invernale del Pistoiese"))
        assertTrue(monsummano.description.contains("Cava Grigia"))
    }

    @Test
    fun `detail page lists every sector in order`() {
        assertEquals(14, monsummano.settori.size)
        assertEquals("Pilastro Rosso", monsummano.settori.first().nome)
        assertEquals("Cava Rossa (destra)", monsummano.settori.last().nome)
        assertTrue(monsummano.settori.all { it.detailUrl.startsWith("https://www.falesiaonline.it/settore") })
    }

    @Test
    fun `sector has id, link, routes, grades, length, approach, exposure, rating and style`() {
        val pilastro = monsummano.settori.first()

        assertEquals(1510L, pilastro.id)
        assertEquals(
            "https://www.falesiaonline.it/settorefoto/1510/pilastro-rosso.html",
            pilastro.detailUrl
        )
        assertEquals(20, pilastro.routeCount)
        assertEquals("6a+", pilastro.gradoMin)
        assertEquals("7c+", pilastro.gradoMax)
        assertEquals("13–28m", pilastro.length)
        assertEquals(6, pilastro.approachMinMinutes)
        assertEquals(15, pilastro.approachMaxMinutes)
        assertEquals(listOf(Exposure.NE), pilastro.exposures)
        assertEquals(4.0, pilastro.rating)
        assertEquals(1, pilastro.ratingCount)
        assertEquals(ViaStyle.Sport, pilastro.style)
    }

    @Test
    fun `sector position comes from its map marker`() {
        val cavaRossaDestra = monsummano.settori.last()

        assertEquals(43.868677704799, cavaRossaDestra.latitude!!, 1e-9)
        assertEquals(10.830212831497, cavaRossaDestra.longitude!!, 1e-9)
        assertTrue(monsummano.settori.all { it.latitude != null && it.longitude != null })
    }

    @Test
    fun `sector with several exposures keeps them all`() {
        val cavaRossaCentro = monsummano.settori.first { it.nome == "Cava Rossa (centro)" }

        assertEquals(listOf(Exposure.SE, Exposure.S, Exposure.SW), cavaRossaCentro.exposures)
    }

    @Test
    fun `crag position is the centre of its sectors`() {
        val lat = monsummano.settori.mapNotNull { it.latitude }.average()
        val lng = monsummano.settori.mapNotNull { it.longitude }.average()

        assertEquals(lat, monsummano.latitude!!, 1e-9)
        assertEquals(lng, monsummano.longitude!!, 1e-9)
    }

    @Test
    fun `page without crag title is a parse failure`() {
        val result = parseDettagliFalesia(html("<p>nothing here</p>"))

        assertTrue(result.leftOrNull() is SyncError.ParseFailure)
    }

    @Test
    fun `minimal crag page has empty values`() {
        val detail = parseDettagliFalesia(html("<h1>Falesia: X</h1>")).getOrNull()!!

        assertNull(detail.pictureUrl)
        assertNull(detail.latitude)
        assertNull(detail.rating)
        assertEquals("", detail.zona)
        assertEquals("", detail.description)
        assertTrue(detail.settori.isEmpty())
    }

    @Test
    fun `sector without votes, grades or approach falls back to defaults`() {
        val detail = parseDettagliFalesia(
            html(
                """<h1>Falesia: X</h1><ul><li class="leafletshadow sectorleaflet">
                <div class="stars"><img src="/images/stelle/star_void.png"/>0</div>
                <h4><a href="/settoredisegno/7/nuovo.html">Nuovo</a></h4>
                <div class="indicatorrow firstrow"><span class="indicator" title="Vie">0</span></div>
                </li><li class="leafletshadow sectorleaflet"><h4><a href="/altro.html">Senza id</a></h4></li></ul>""",
            ),
        ).getOrNull()!!

        val settore = detail.settori.single()
        assertEquals(7L, settore.id)
        assertEquals("", settore.gradoMin)
        assertNull(settore.approachMinMinutes)
        assertNull(settore.rating)
        assertEquals(0, settore.ratingCount)
        assertEquals(ViaStyle.Unknown, settore.style)
        assertNull(settore.latitude)
    }

    // ── Sector page (saved page of a sector with 16 routes) ──

    private val sectorRoutes by lazy { parseVieSettore(fixture("SectorPage.html")).getOrNull()!! }

    @Test
    fun `sector page yields every route in order`() {
        assertEquals(16, sectorRoutes.size)
        assertEquals("Sanguisughe", sectorRoutes.first().nome)
        assertEquals("Rimmel", sectorRoutes.last().nome)
    }

    @Test
    fun `route has id, French grade, rating, votes, length and style`() {
        val first = sectorRoutes.first()

        assertEquals(9899L, first.id)
        assertEquals("6c", first.grado)
        assertEquals(3.0, first.rating)
        assertEquals(5, first.ratingCount)
        assertEquals("15–20m", first.length)
        assertEquals(ViaStyle.Sport, first.style)
    }

    @Test
    fun `route stars of the list row ignore the detail pane`() {
        // Rimmel is followed by the route detail pane, which repeats the stars of every route.
        val rimmel = sectorRoutes.last()

        assertEquals(2.5, rimmel.rating)
        assertEquals(3, rimmel.ratingCount)
    }

    @Test
    fun `route with a warning counter keeps grade and length`() {
        val autoerotismo = sectorRoutes.first { it.nome == "Autoerotismo" }

        assertEquals("5a", autoerotismo.grado)
        assertEquals("18–22m", autoerotismo.length)
    }

    @Test
    fun `page without route list is a parse failure`() {
        val result = parseVieSettore(html("<p>topo only</p>"))

        assertTrue(result.leftOrNull() is SyncError.ParseFailure)
    }

    // ── Sector topo (SVG of the sector page) ──

    private val sectorTopo by lazy { parseTopo(fixture("SectorPage.html"))!! }

    /** A multi-pitch sector with abseils and descents (Monsummano, Cava Grigia), saved for the debug build. */
    private val multiPitchPage by lazy {
        Ksoup.parse(File("src/debug/assets/fixtures/settore/1090.html").readText())
    }

    @Test
    fun `topo has the viewBox and the photo placed by its transform matrix`() {
        assertEquals(0.0, sectorTopo.viewBoxX, 0.0)
        assertEquals(936.141, sectorTopo.viewBoxWidth, 0.0)
        assertEquals(528.686, sectorTopo.viewBoxHeight, 0.0)
        assertEquals(
            "https://www.falesiaonline.it/uploads/assets/image/159130980856Parete-principale.jpg",
            sectorTopo.imageUrl,
        )
        // 2050 × 1158 px scaled by 0.45665414634146: the photo fills the viewBox.
        assertEquals(936.141, sectorTopo.imageWidth, 0.001)
        assertEquals(528.805, sectorTopo.imageHeight, 0.001)
        assertEquals(0.0, sectorTopo.imageX, 0.0)
    }

    @Test
    fun `topo has one line per route, with the route id and its path`() {
        assertEquals(16, sectorTopo.lines.size)
        assertEquals(sectorRoutes.map { it.id }.toSet(), sectorTopo.lines.map { it.viaId }.toSet())
        val first = sectorTopo.lines.first()
        assertEquals(9899L, first.viaId)
        assertEquals(1, first.segment)
        assertTrue(first.pathData.startsWith("M38.07,284.543c0,0-6.5,30-12.5,48.5"))
    }

    @Test
    fun `multi-pitch routes have one segment per pitch and abseils are left out`() {
        val topo = parseTopo(multiPitchPage)!!

        assertEquals(listOf(1, 2, 3), topo.lines.filter { it.viaId == 11014L }.map { it.segment })
        assertEquals(
            listOf(1, 2, 3, 4),
            topo.lines.filter { it.viaId == 11020L }.map { it.segment })
        // 11015 and 11017 are abseils: hidden on the site.
        assertFalse(topo.lines.any { it.viaId == 11015L || it.viaId == 11017L })
    }

    @Test
    fun `descents in the route list are not routes`() {
        val vie = parseVieSettore(multiPitchPage).getOrNull()!!

        assertFalse(vie.any { it.id == 11015L || it.grado == "Calata" || it.nome.isBlank() })
        assertTrue(vie.any { it.id == 11014L && it.nome == "Rossano picchia piano" })
    }

    @Test
    fun `sector page gives routes and topo together`() {
        val page = parseSettore(fixture("SectorPage.html")).getOrNull()!!

        assertEquals(16, page.vie.size)
        assertNotNull(page.topo)
    }

    @Test
    fun `page without topo has none and is still a sector page`() {
        val page = parseSettore(html("""<div id="routes_list"></div>""")).getOrNull()!!

        assertNull(page.topo)
        assertTrue(page.vie.isEmpty())
    }

    @Test
    fun `topo with an unreadable viewBox is ignored`() {
        assertNull(parseTopo(html("""<svg id="topoSvgContent" viewBox="0 0 abc"></svg>""")))
        assertNull(parseTopo(html("""<svg id="topoSvgContent" viewBox="0 0 0 300"></svg>""")))
    }

    @Test
    fun `photo without a readable matrix fills the viewBox, empty and unnamed paths are skipped`() {
        val topo = parseTopo(
            html(
                """
                <svg id="topoSvgContent" viewBox="0 0 400 300">
                  <g id="Foto"><image xlink:href="/uploads/wall.jpg" transform="rotate(3)"></image></g>
                  <g id="Route_Paths">
                    <path id="route_7_1" d="M0,0L10,10"></path>
                    <path id="route_8_1" d=" "></path>
                    <path id="route_x_1" d="M0,0L5,5"></path>
                    <path id="route_9_1" style="display: none;" d="M0,0L5,5"></path>
                  </g>
                </svg>
                """.trimIndent(),
            ),
        )!!

        assertEquals("https://www.falesiaonline.it/uploads/wall.jpg", topo.imageUrl)
        assertEquals(400.0, topo.imageWidth, 0.0)
        assertEquals(300.0, topo.imageHeight, 0.0)
        assertEquals(listOf(7L), topo.lines.map { it.viaId })
    }

    // ── Crag map page ──

    @Test
    fun `map page has the parking`() {
        assertEquals(
            42.884588547781 to 11.579203605652,
            parseParcheggio(fixture("CragMapPage.html"))
        )
    }

    @Test
    fun `map page without parking returns null`() {
        assertNull(parseParcheggio(html("<script>var map = 1;</script>")))
    }

    @Test
    fun `map url is derived from the detail url`() {
        assertEquals(
            "https://www.falesiaonline.it/falesia/map/monsummano.html",
            mapUrlFromDetailUrl("https://www.falesiaonline.it/falesia/monsummano.html"),
        )
    }

    // ── Exposures ──

    @Test
    fun `Italian exposures are mapped to compass directions`() {
        assertEquals(listOf(Exposure.SW, Exposure.W), parseExposures("SO-O"))
        assertEquals(listOf(Exposure.NE, Exposure.E), parseExposures("NE-E"))
        assertEquals(listOf(Exposure.NW, Exposure.N), parseExposures("NO-N-NO"))
        assertTrue(parseExposures("").isEmpty())
        assertTrue(parseExposures("variabile").isEmpty())
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
