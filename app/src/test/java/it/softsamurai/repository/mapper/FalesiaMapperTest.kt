package it.softsamurai.repository.mapper

import it.softsamurai.domain.model.Exposure
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.GeoPoint
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.SettoreNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FalesiaMapperTest {

    // ── FalesiaWithVie → Domain ──────────────────────────────────────────

    @Test
    fun `FalesiaWithVie toDomain maps all fields correctly`() {
        val entity = fakeEntity()
        val withVie = FalesiaWithVie(
            entity,
            listOf(fakeSettoreEntity(1, position = 0)),
            listOf(
                fakeViaEntity(1L, settoreId = 1, position = 0),
                fakeViaEntity(2L, settoreId = 1, position = 1)
            ),
        )

        val domain = withVie.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.nome, domain.nome)
        assertEquals(entity.zona, domain.zona)
        assertEquals(entity.pictureUrl, domain.pictureUrl)
        assertEquals(entity.latitude, domain.latitude)
        assertEquals(entity.longitude, domain.longitude)
        assertEquals(entity.numeroVie, domain.numeroVie)
        assertEquals(entity.linkDettagli, domain.linkDettagli)
        assertEquals(entity.lastUpdated, domain.lastUpdated)
        assertEquals(entity.description, domain.description)
        assertEquals(entity.rating, domain.rating)
        assertEquals(entity.ratingCount, domain.ratingCount)
        assertEquals(entity.rock, domain.rock)
        assertEquals(entity.seasons, domain.seasons)
        assertEquals(GeoPoint(45.9, 10.8), domain.parking)
        assertEquals(entity.vieUpdatedAt, domain.vieUpdatedAt)
        assertEquals(2, domain.vie.size)
        assertEquals("Via 1", domain.vie[0].nome)
        assertEquals(1, domain.settori.size)
    }

    @Test
    fun `FalesiaWithVie toDomain sorts sectors and routes as on the site`() {
        val withVie = FalesiaWithVie(
            fakeEntity(),
            listOf(fakeSettoreEntity(20, position = 1), fakeSettoreEntity(10, position = 0)),
            listOf(
                fakeViaEntity(3L, settoreId = 20, position = 0),
                fakeViaEntity(2L, settoreId = 10, position = 1),
                fakeViaEntity(1L, settoreId = 10, position = 0),
            ),
        )

        val domain = withVie.toDomain()

        assertEquals(listOf(10L, 20L), domain.settori.map { it.id })
        assertEquals(listOf(1L, 2L, 3L), domain.vie.map { it.id })
    }

    @Test
    fun `FalesiaWithVie toDomain without parking and routes`() {
        val entity = fakeEntity().copy(parkingLatitude = null, vieUpdatedAt = null)
        val domain = FalesiaWithVie(entity, emptyList(), emptyList()).toDomain()

        assertEquals(emptyList<Via>(), domain.vie)
        assertNull(domain.parking)
        assertNull(domain.vieUpdatedAt)
    }

    @Test
    fun `SettoreEntity toDomain decodes exposures and style`() {
        val settore =
            fakeSettoreEntity(1, position = 0).copy(exposures = "SW,S,bogus", style = "Trad")
                .toDomain()

        assertEquals(listOf(Exposure.SW, Exposure.S), settore.exposures)
        assertEquals(ViaStyle.Trad, settore.style)
        assertEquals(GeoPoint(43.8, 10.8), settore.location)
    }

    @Test
    fun `unknown style name becomes Unknown`() {
        assertEquals(
            ViaStyle.Unknown,
            fakeViaEntity(1, settoreId = 1, position = 0).copy(style = "x").toDomain().style
        )
    }

    // ── Domain → Entity ──────────────────────────────────────────────────

    @Test
    fun `Falesia toEntity round trips through FalesiaWithVie`() {
        val falesia = fakeDomain()

        val roundTrip = FalesiaWithVie(
            falesia.toEntity(),
            falesia.settori.mapIndexed { index, settore -> settore.toEntity(falesia.id, index) },
            falesia.vie.mapIndexed { index, via -> via.toEntity(falesia.id, index) },
        ).toDomain()

        assertEquals(falesia, roundTrip)
    }

    // ── Network → Entity ─────────────────────────────────────────────────

    @Test
    fun `idFromUrl extracts last path segment without extension`() {
        val listItem =
            fakeListItem(detailUrl = "https://www.falesiaonline.it/falesia/monsummano.html")
        assertEquals("monsummano", listItem.idFromUrl())
    }

    @Test
    fun `idFromUrl handles a path without extension and a trailing slash`() {
        assertEquals(
            "arco",
            fakeListItem(detailUrl = "https://www.falesiaonline.it/falesia/arco").idFromUrl()
        )
        assertEquals(
            "arco",
            fakeListItem(detailUrl = "https://www.falesiaonline.it/falesia/arco/").idFromUrl()
        )
    }

    @Test
    fun `FalesiaListItemNetworkModel toEntity uses detail pictureUrl when available`() {
        val listItem = fakeListItem(listPictureUrl = "https://list.img/thumb.jpg")
        val detail = fakeDetail(detailPictureUrl = "https://detail.img/main.jpg")

        val entity = listItem.toEntity(detail, now = 1000L)

        assertEquals("https://detail.img/main.jpg", entity.pictureUrl)
    }

    @Test
    fun `FalesiaListItemNetworkModel toEntity falls back to list pictureUrl when detail has none`() {
        val listItem = fakeListItem(listPictureUrl = "https://list.img/thumb.jpg")
        val detail = fakeDetail(detailPictureUrl = null)

        val entity = listItem.toEntity(detail, now = 1000L)

        assertEquals("https://list.img/thumb.jpg", entity.pictureUrl)
    }

    @Test
    fun `FalesiaListItemNetworkModel toEntity takes zone, description and rating from the detail`() {
        val entity = fakeListItem().toEntity(fakeDetail(), now = 42_000L)

        assertEquals(42_000L, entity.lastUpdated)
        assertEquals("Garda, Trentino", entity.zona)
        assertEquals("Bella", entity.description)
        assertEquals(4.5, entity.rating)
        assertEquals(12, entity.ratingCount)
        assertEquals("Calcare", entity.rock)
        assertEquals("Estate", entity.seasons)
    }

    @Test
    fun `FalesiaListItemNetworkModel toEntity stores null coords when detail has none`() {
        val entity = fakeListItem().toEntity(fakeDetail(lat = null, lng = null), now = 0L)
        assertNull(entity.latitude)
        assertNull(entity.longitude)
    }

    @Test
    fun `SettoreNetworkModel toEntity encodes exposures and style`() {
        val entity = SettoreNetworkModel(
            id = 7,
            nome = "Paretone",
            detailUrl = "https://x/settorefoto/7/p.html",
            routeCount = 20,
            gradoMin = "5a",
            gradoMax = "7b",
            length = "8–40m",
            approachMinMinutes = 12,
            approachMaxMinutes = 21,
            exposures = listOf(Exposure.SW, Exposure.W),
            rating = 4.5,
            ratingCount = 9,
            style = ViaStyle.Sport,
            latitude = 43.8,
            longitude = 10.8,
        ).toEntity("arco", position = 3)

        assertEquals(7L, entity.id)
        assertEquals("arco", entity.falesiaId)
        assertEquals(3, entity.position)
        assertEquals("SW,W", entity.exposures)
        assertEquals("Sport", entity.style)
        assertEquals("https://x/settorefoto/7/p.html", entity.linkDettagli)
    }

    @Test
    fun `ViaNetworkModel toEntity maps correctly`() {
        val net = ViaNetworkModel(
            id = 9899,
            nome = "Sole",
            grado = "7b",
            rating = 3.0,
            ratingCount = 5,
            length = "15–20m",
            style = ViaStyle.Sport,
        )
        val entity = net.toEntity("arco", settoreId = 7, position = 2)

        assertEquals(9899L, entity.id)
        assertEquals("arco", entity.falesiaId)
        assertEquals(7L, entity.settoreId)
        assertEquals(2, entity.position)
        assertEquals("Sole", entity.nome)
        assertEquals("7b", entity.grado)
        assertEquals(3.0, entity.rating)
        assertEquals("15–20m", entity.length)
        assertEquals("Sport", entity.style)
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun fakeEntity() = FalesiaEntity(
        id = "arco",
        nome = "Arco",
        zona = "Trentino",
        pictureUrl = "https://example.com/arco.jpg",
        latitude = 45.92,
        longitude = 10.88,
        numeroVie = 200,
        linkDettagli = "https://www.falesiaonline.it/falesia/arco.html",
        lastUpdated = 1_000_000L,
        description = "Bella",
        rating = 4.0,
        ratingCount = 3,
        rock = "Calcare",
        seasons = "Estate",
        parkingLatitude = 45.9,
        parkingLongitude = 10.8,
        vieUpdatedAt = 2_000_000L,
    )

    private fun fakeSettoreEntity(id: Long, position: Int) = SettoreEntity(
        id = id,
        falesiaId = "arco",
        position = position,
        nome = "Settore $id",
        numeroVie = 5,
        gradoMin = "5a",
        gradoMax = "7a",
        length = "20m",
        approachMinMinutes = 5,
        approachMaxMinutes = 10,
        exposures = "SW",
        rating = 4.0,
        ratingCount = 2,
        style = "Sport",
        latitude = 43.8,
        longitude = 10.8,
        linkDettagli = "https://www.falesiaonline.it/settorefoto/$id/s.html",
    )

    private fun fakeViaEntity(id: Long, settoreId: Long, position: Int) = ViaEntity(
        id = id,
        falesiaId = "arco",
        settoreId = settoreId,
        position = position,
        nome = "Via $id",
        grado = "6a",
        rating = 3.5,
        ratingCount = 4,
        length = "20m",
        style = "Sport",
    )

    private fun fakeDomain() = Falesia(
        id = "arco",
        nome = "Arco",
        zona = "Trentino",
        pictureUrl = "https://example.com/arco.jpg",
        latitude = 45.92,
        longitude = 10.88,
        numeroVie = 200,
        linkDettagli = "https://www.falesiaonline.it/falesia/arco.html",
        vie = listOf(
            Via(
                id = 1,
                nome = "Spigolo",
                grado = "6b",
                settoreId = 7,
                rating = 4.0,
                ratingCount = 2,
                length = "25m",
                style = ViaStyle.Sport
            ),
        ),
        lastUpdated = 1_000_000L,
        description = "Bella",
        rating = 4.5,
        ratingCount = 12,
        rock = "Calcare",
        seasons = "Estate",
        parking = GeoPoint(45.9, 10.8),
        settori = listOf(
            Settore(
                id = 7,
                nome = "Paretone",
                numeroVie = 20,
                gradoMin = "5a",
                gradoMax = "7b",
                length = "8–40m",
                approachMinMinutes = 12,
                approachMaxMinutes = 21,
                exposures = listOf(Exposure.SW),
                rating = 4.5,
                ratingCount = 9,
                style = ViaStyle.Sport,
                location = GeoPoint(43.8, 10.8),
                linkDettagli = "https://x/settorefoto/7/p.html",
            ),
        ),
        vieUpdatedAt = 2_000_000L,
    )

    private fun fakeListItem(
        nome: String = "Arco",
        listPictureUrl: String? = null,
        routeCount: Int = 200,
        detailUrl: String = "https://www.falesiaonline.it/falesia/arco.html",
    ) = FalesiaListItemNetworkModel(nome, listPictureUrl, routeCount, detailUrl)

    private fun fakeDetail(
        detailPictureUrl: String? = "https://detail.com/img.jpg",
        lat: Double? = 45.92,
        lng: Double? = 10.88,
    ) = FalesiaDetailNetworkModel(
        pictureUrl = detailPictureUrl,
        latitude = lat,
        longitude = lng,
        zona = "Garda, Trentino",
        description = "Bella",
        rating = 4.5,
        ratingCount = 12,
        rock = "Calcare",
        seasons = "Estate",
    )
}
