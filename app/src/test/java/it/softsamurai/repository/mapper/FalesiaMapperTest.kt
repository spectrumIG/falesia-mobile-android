package it.softsamurai.repository.mapper

import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FalesiaMapperTest {

    // ── FalesiaWithVie → Domain ──────────────────────────────────────────

    @Test
    fun `FalesiaWithVie toDomain maps all fields correctly`() {
        val entity = fakeEntity()
        val vieEntities = listOf(fakeViaEntity(1L, entity.id), fakeViaEntity(2L, entity.id))
        val withVie = FalesiaWithVie(entity, vieEntities)

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
        assertEquals(2, domain.vie.size)
        assertEquals("Via 1", domain.vie[0].nome)
    }

    @Test
    fun `FalesiaWithVie toDomain maps empty vie list`() {
        val withVie = FalesiaWithVie(fakeEntity(), emptyList())
        val domain = withVie.toDomain()
        assertEquals(emptyList<Via>(), domain.vie)
    }

    // ── Domain → Entity ──────────────────────────────────────────────────

    @Test
    fun `Falesia toEntity maps all fields correctly`() {
        val falesia = fakeDomain()
        val entity = falesia.toEntity()

        assertEquals(falesia.id, entity.id)
        assertEquals(falesia.nome, entity.nome)
        assertEquals(falesia.zona, entity.zona)
        assertEquals(falesia.pictureUrl, entity.pictureUrl)
        assertEquals(falesia.latitude, entity.latitude)
        assertEquals(falesia.longitude, entity.longitude)
        assertEquals(falesia.numeroVie, entity.numeroVie)
        assertEquals(falesia.linkDettagli, entity.linkDettagli)
        assertEquals(falesia.lastUpdated, entity.lastUpdated)
    }

    @Test
    fun `Via toEntity maps all fields correctly`() {
        val via = Via(id = 5L, nome = "Girasole", grado = "6a")
        val entity = via.toEntity("arco")

        assertEquals(5L, entity.id)
        assertEquals("arco", entity.falesiaId)
        assertEquals("Girasole", entity.nome)
        assertEquals("6a", entity.grado)
    }

    // ── Network → Entity ─────────────────────────────────────────────────

    @Test
    fun `idFromUrl extracts last path segment`() {
        val listItem = fakeListItem(detailUrl = "https://www.falesiaonline.it/falesia/arco")
        assertEquals("arco", listItem.idFromUrl())
    }

    @Test
    fun `idFromUrl handles trailing slash`() {
        val listItem = fakeListItem(detailUrl = "https://www.falesiaonline.it/falesia/arco/")
        assertEquals("arco", listItem.idFromUrl())
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
    fun `FalesiaListItemNetworkModel toEntity sets lastUpdated from now parameter`() {
        val entity = fakeListItem().toEntity(fakeDetail(), now = 42_000L)
        assertEquals(42_000L, entity.lastUpdated)
    }

    @Test
    fun `ViaNetworkModel toEntity maps correctly`() {
        val net = ViaNetworkModel(nome = "Sole", grado = "7b")
        val entity = net.toEntity("arco")

        assertEquals("arco", entity.falesiaId)
        assertEquals("Sole", entity.nome)
        assertEquals("7b", entity.grado)
        assertEquals(0L, entity.id) // auto-generate sentinel
    }

    @Test
    fun `FalesiaListItemNetworkModel toEntity stores null coords when detail has none`() {
        val entity = fakeListItem().toEntity(fakeDetail(lat = null, lng = null), now = 0L)
        assertNull(entity.latitude)
        assertNull(entity.longitude)
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
        linkDettagli = "https://www.falesiaonline.it/falesia/arco",
        lastUpdated = 1_000_000L,
    )

    private fun fakeViaEntity(id: Long, falesiaId: String) = ViaEntity(
        id = id,
        falesiaId = falesiaId,
        nome = "Via $id",
        grado = "6a",
    )

    private fun fakeDomain() = Falesia(
        id = "arco",
        nome = "Arco",
        zona = "Trentino",
        pictureUrl = "https://example.com/arco.jpg",
        latitude = 45.92,
        longitude = 10.88,
        numeroVie = 200,
        linkDettagli = "https://www.falesiaonline.it/falesia/arco",
        vie = emptyList(),
        lastUpdated = 1_000_000L,
    )

    private fun fakeListItem(
        nome: String = "Arco",
        listPictureUrl: String? = null,
        routeCount: Int = 200,
        detailUrl: String = "https://www.falesiaonline.it/falesia/arco",
    ) = FalesiaListItemNetworkModel(nome, listPictureUrl, routeCount, detailUrl)

    private fun fakeDetail(
        detailPictureUrl: String? = "https://detail.com/img.jpg",
        lat: Double? = 45.92,
        lng: Double? = 10.88,
        vie: List<ViaNetworkModel> = emptyList(),
    ) = FalesiaDetailNetworkModel(detailPictureUrl, lat, lng, vie)
}
