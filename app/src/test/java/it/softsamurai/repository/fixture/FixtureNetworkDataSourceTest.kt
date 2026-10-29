package it.softsamurai.repository.fixture

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import it.softsamurai.domain.error.SyncError
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.FalesiaSyncEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.TopoEntity
import it.softsamurai.repository.database.entity.TopoLineEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.local.FalesiaLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Saved pages of src/test/resources standing in for the debug assets. */
private val testFixtures = mapOf(
    "elenco.html" to "AllCragsResponse.html",
    "falesia/monsummano.html" to "SingleCragPage.html",
    "map/monsummano.html" to "CragMapPage.html",
    "settore/1510.html" to "SectorPage.html",
)

class FixtureNetworkDataSourceTest {

    private val reader = FixtureReader { path ->
        testFixtures[path]?.let { requireNotNull(javaClass.classLoader?.getResource(it)).readText() }
    }
    private val source = FixtureNetworkDataSource(reader)

    // ── Data source ──

    @Test
    fun `crag list keeps only the crags with a saved page`() = runTest {
        val crags = source.fetchElencoFalesie().getOrNull()!!

        assertEquals(listOf("Monsummano"), crags.map { it.nome })
    }

    @Test
    fun `crag page, sector page and parking are read by url`() = runTest {
        val detail = source.fetchDettagliFalesia("https://www.falesiaonline.it/falesia/monsummano.html").getOrNull()!!
        val settore =
            source.fetchSettore("https://www.falesiaonline.it/settorefoto/1510/pilastro-rosso.html")
                .getOrNull()!!
        val parking = source.fetchParcheggio("https://www.falesiaonline.it/falesia/monsummano.html")

        assertEquals(14, detail.settori.size)
        assertEquals(16, settore.vie.size)
        assertEquals(16, settore.topo!!.lines.size)
        assertEquals(42.884588547781 to 11.579203605652, parking.getOrNull())
    }

    @Test
    fun `a sector without saved page has no routes and no topo, a crag without one is a parse failure`() =
        runTest {
            val settore = source.fetchSettore("https://www.falesiaonline.it/settorefoto/12/x.html")
                .getOrNull()!!
            assertTrue(settore.vie.isEmpty())
            assertNull(settore.topo)
        assertNull(source.fetchParcheggio("https://www.falesiaonline.it/falesia/arco.html").getOrNull())
        assertTrue(source.fetchDettagliFalesia("https://www.falesiaonline.it/falesia/arco.html").leftOrNull() is SyncError.ParseFailure)
    }

    // ── Loader ──

    @Test
    fun `loader does nothing when the db has data`() = runTest {
        val local = mockk<FalesiaLocalDataSource>()
        coEvery { local.countFalesie() } returns 3

        val result = FixtureDataLoader(local, source, Dispatchers.Unconfined).loadIfEmpty()

        assertTrue(result.isRight())
        coVerify(exactly = 0) { local.upsertFalesiaFromSync(any(), any()) }
    }

    @Test
    fun `loader fills an empty db with crag, sectors, routes, topi and parking marked as never synced`() =
        runTest {
        val local = mockk<FalesiaLocalDataSource>()
        val crag = slot<FalesiaSyncEntity>()
        val settori = slot<List<SettoreEntity>>()
        val vie = slot<List<ViaEntity>>()
            val topi = slot<List<TopoEntity>>()
            val lines = slot<List<TopoLineEntity>>()
        coEvery { local.countFalesie() } returns 0
        coEvery { local.upsertFalesiaFromSync(capture(crag), capture(settori)) } returns Unit
        coEvery { local.getFalesiaWithVieById("monsummano") } answers {
            FalesiaWithVie(crag.captured.toEntity(), settori.captured, emptyList())
        }
            coEvery {
                local.replaceDettagli(
                    "monsummano",
                    capture(vie),
                    capture(topi),
                    capture(lines),
                    any(),
                    any()
                )
            } returns Unit

        val result = FixtureDataLoader(local, source, Dispatchers.Unconfined).loadIfEmpty()

        assertTrue(result.isRight())
        assertEquals("monsummano", crag.captured.id)
        assertEquals(0L, crag.captured.lastUpdated)
        assertEquals(14, settori.captured.size)
        assertEquals(16, vie.captured.size)
            assertEquals(listOf(1510L), topi.captured.map { it.settoreId })
            assertEquals(16, lines.captured.size)
            coVerify {
                local.replaceDettagli(
                    "monsummano",
                    any(),
                    any(),
                    any(),
                    42.884588547781 to 11.579203605652,
                    0L
                )
            }
    }

    private fun FalesiaSyncEntity.toEntity() = FalesiaEntity(
        id = id, nome = nome, zona = zona, pictureUrl = pictureUrl, latitude = latitude, longitude = longitude,
        numeroVie = numeroVie, linkDettagli = linkDettagli, lastUpdated = lastUpdated,
    )
}
