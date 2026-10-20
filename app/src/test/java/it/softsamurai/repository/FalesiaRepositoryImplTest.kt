package it.softsamurai.repository

import app.cash.turbine.test
import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Settore
import it.softsamurai.domain.model.Via
import it.softsamurai.domain.model.ViaStyle
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.entity.FalesiaSyncEntity
import it.softsamurai.repository.database.entity.SettoreEntity
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.local.FalesiaLocalDataSource
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.SettoreNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val NOW = 1_700_000_000_000L

class FalesiaRepositoryImplTest {

    private lateinit var localDataSource: FalesiaLocalDataSource
    private lateinit var networkDataSource: FalesiaNetworkDataSource
    private lateinit var repository: FalesiaRepositoryImpl

    @Before
    fun setUp() {
        localDataSource = mockk()
        networkDataSource = mockk()
        repository = FalesiaRepositoryImpl(localDataSource, networkDataSource, Dispatchers.Unconfined) { NOW }
    }

    // ── observeAllFalesie ─────────────────────────────────────────────────

    @Test
    fun `observeAllFalesie maps entities to domain models`() = runTest {
        val entities = listOf(fakeWithVie("arco"), fakeWithVie("finale"))
        every { localDataSource.observeAllFalesieWithVie() } returns flowOf(entities)

        repository.observeAllFalesie().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("arco", list[0].id)
            assertEquals("finale", list[1].id)
            awaitComplete()
        }
    }

    // ── getFalesiaById ────────────────────────────────────────────────────

    @Test
    fun `getFalesiaById returns Right when crag exists`() = runTest {
        coEvery { localDataSource.getFalesiaWithVieById("arco") } returns fakeWithVie("arco")

        val result = repository.getFalesiaById("arco")

        assertTrue(result.isRight())
        assertEquals("arco", (result as Either.Right).value.id)
    }

    @Test
    fun `getFalesiaById returns Left NotFound when crag is missing`() = runTest {
        coEvery { localDataSource.getFalesiaWithVieById("unknown") } returns null

        val result = repository.getFalesiaById("unknown")

        assertTrue(result.isLeft())
        val error = (result as Either.Left).value
        assertTrue(error is FalesiaError.NotFound)
        assertEquals("unknown", (error as FalesiaError.NotFound).id)
    }

    // ── upsertFalesia ─────────────────────────────────────────────────────

    @Test
    fun `upsertFalesia returns Right on success`() = runTest {
        val falesia = fakeDomain("arco")
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any(), any()) } returns Unit

        val result = repository.upsertFalesia(falesia)

        assertTrue(result.isRight())
        coVerify { localDataSource.upsertFalesiaWithVie(any(), any(), any()) }
    }

    @Test
    fun `upsertFalesia returns Left DatabaseError when exception thrown`() = runTest {
        coEvery {
            localDataSource.upsertFalesiaWithVie(
                any(),
                any(),
                any()
            )
        } throws RuntimeException("db fail")

        val result = repository.upsertFalesia(fakeDomain("arco"))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is FalesiaError.DatabaseError)
    }

    // ── deleteFalesia ─────────────────────────────────────────────────────

    @Test
    fun `deleteFalesia returns Right on success`() = runTest {
        coEvery { localDataSource.deleteFalesiaById("arco") } returns Unit

        val result = repository.deleteFalesia("arco")

        assertTrue(result.isRight())
    }

    @Test
    fun `deleteFalesia returns Left DatabaseError when exception thrown`() = runTest {
        coEvery { localDataSource.deleteFalesiaById("arco") } throws RuntimeException("disk full")

        val result = repository.deleteFalesia("arco")

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is FalesiaError.DatabaseError)
    }

    // ── syncFalesie ───────────────────────────────────────────────────────

    @Test
    fun `syncFalesie returns Right on full success`() = runTest {
        val listItems = listOf(fakeListItem("arco"), fakeListItem("finale"))
        val detail = fakeDetail()
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Right(listItems)
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns Either.Right(detail)
        coEvery { localDataSource.upsertFalesiaFromSync(any(), any()) } returns Unit

        val result = repository.syncFalesie()

        assertTrue(result.isRight())
        coVerify(exactly = 2) { localDataSource.upsertFalesiaFromSync(any(), any()) }
    }

    @Test
    fun `syncFalesie returns Left NetworkFailure when list fetch fails`() = runTest {
        val error = SyncError.NetworkFailure(RuntimeException("offline"))
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Left(error)

        val result = repository.syncFalesie()

        assertTrue(result.isLeft())
        assertEquals(error, (result as Either.Left).value)
    }

    @Test
    fun `syncFalesie returns Left NetworkFailure when detail fetch fails`() = runTest {
        val error = SyncError.NetworkFailure(RuntimeException("timeout"))
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Right(listOf(fakeListItem("arco")))
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns Either.Left(error)

        val result = repository.syncFalesie()

        assertTrue(result.isLeft())
        assertEquals(error, (result as Either.Left).value)
    }

    @Test
    fun `syncFalesie returns Left StorageFailure when DB write fails`() = runTest {
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Right(listOf(fakeListItem("arco")))
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns Either.Right(fakeDetail())
        coEvery {
            localDataSource.upsertFalesiaFromSync(
                any(),
                any()
            )
        } throws RuntimeException("disk full")

        val result = repository.syncFalesie()

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is SyncError.StorageFailure)
    }

    @Test
    fun `syncFalesie stores every crag with id from url, its sectors in order and the sync time`() =
        runTest {
        coEvery { networkDataSource.fetchElencoFalesie() } returns
            Either.Right(listOf(fakeListItem("arco"), fakeListItem("finale")))
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns
                Either.Right(
                    fakeDetail(
                        settori = listOf(
                            fakeSettoreNetwork(7),
                            fakeSettoreNetwork(3)
                        )
                    )
                )
            val saved = mutableListOf<Pair<FalesiaSyncEntity, List<SettoreEntity>>>()
            val entity = slot<FalesiaSyncEntity>()
            val settori = slot<List<SettoreEntity>>()
            coEvery {
                localDataSource.upsertFalesiaFromSync(
                    capture(entity),
                    capture(settori)
                )
            } answers {
                saved += entity.captured to settori.captured
        }

        repository.syncFalesie()

        assertEquals(listOf("arco", "finale"), saved.map { it.first.id })
        assertTrue(saved.all { it.first.lastUpdated == NOW })
            assertEquals(listOf(7L, 3L), saved.first().second.map { it.id })
            assertEquals(listOf(0, 1), saved.first().second.map { it.position })
            assertEquals(listOf("finale", "finale"), saved.last().second.map { it.falesiaId })
        }

    @Test
    fun `syncFalesie never touches the on-demand routes`() = runTest {
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Right(
            listOf(
                fakeListItem(
                    "arco"
                )
            )
        )
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns Either.Right(fakeDetail())
        coEvery { localDataSource.upsertFalesiaFromSync(any(), any()) } returns Unit

        repository.syncFalesie()

        coVerify(exactly = 0) { localDataSource.upsertFalesiaWithVie(any(), any(), any()) }
        coVerify(exactly = 0) { localDataSource.replaceDettagli(any(), any(), any(), any()) }
    }

    @Test
    fun `syncFalesie fetches the details of every crag beyond the concurrency chunk`() = runTest {
        val ids = (1..12).map { "crag$it" }
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Right(ids.map { fakeListItem(it) })
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns Either.Right(fakeDetail())
        coEvery { localDataSource.upsertFalesiaFromSync(any(), any()) } returns Unit

        val result = repository.syncFalesie()

        assertTrue(result.isRight())
        ids.forEach { id ->
            coVerify(exactly = 1) { networkDataSource.fetchDettagliFalesia("https://www.falesiaonline.it/falesia/$id") }
        }
        coVerify(exactly = 12) { localDataSource.upsertFalesiaFromSync(any(), any()) }
    }

    @Test
    fun `syncFalesie saves nothing when one detail page fails`() = runTest {
        coEvery { networkDataSource.fetchElencoFalesie() } returns
            Either.Right(listOf(fakeListItem("arco"), fakeListItem("finale")))
        coEvery { networkDataSource.fetchDettagliFalesia(match { it.endsWith("arco") }) } returns
            Either.Right(fakeDetail())
        coEvery { networkDataSource.fetchDettagliFalesia(match { it.endsWith("finale") }) } returns
            Either.Left(SyncError.NetworkFailure(RuntimeException("timeout")))

        val result = repository.syncFalesie()

        assertTrue(result.isLeft())
        coVerify(exactly = 0) { localDataSource.upsertFalesiaFromSync(any(), any()) }
    }

    // ── refreshCragDetails ────────────────────────────────────────────────

    @Test
    fun `refreshCragDetails stores the routes of every sector, the parking and the time`() =
        runTest {
            coEvery { localDataSource.getFalesiaWithVieById("arco") } returns
                    fakeWithVie(
                        "arco",
                        settori = listOf(
                            fakeSettoreEntity(2, position = 1),
                            fakeSettoreEntity(1, position = 0)
                        )
                    )
            coEvery { networkDataSource.fetchVieSettore("https://www.falesiaonline.it/settorefoto/1/s.html") } returns
                    Either.Right(listOf(fakeViaNetwork(10), fakeViaNetwork(11)))
            coEvery { networkDataSource.fetchVieSettore("https://www.falesiaonline.it/settorefoto/2/s.html") } returns
                    Either.Right(listOf(fakeViaNetwork(20)))
            coEvery { networkDataSource.fetchParcheggio("https://example.com/arco") } returns Either.Right(
                45.0 to 10.0
            )
            val vie = slot<List<ViaEntity>>()
            coEvery {
                localDataSource.replaceDettagli(
                    "arco",
                    capture(vie),
                    45.0 to 10.0,
                    NOW
                )
            } returns Unit

            val result = repository.refreshCragDetails("arco")

            assertTrue(result.isRight())
            assertEquals(listOf(10L, 11L, 20L), vie.captured.map { it.id })
            assertEquals(listOf(1L, 1L, 2L), vie.captured.map { it.settoreId })
            assertEquals(listOf(0, 1, 0), vie.captured.map { it.position })
            assertTrue(vie.captured.all { it.falesiaId == "arco" })
        }

    @Test
    fun `refreshCragDetails stores a route listed in two sectors once`() = runTest {
        coEvery { localDataSource.getFalesiaWithVieById("arco") } returns
                fakeWithVie(
                    "arco",
                    settori = listOf(
                        fakeSettoreEntity(1, position = 0),
                        fakeSettoreEntity(2, position = 1)
                    )
                )
        coEvery { networkDataSource.fetchVieSettore(any()) } returns Either.Right(
            listOf(
                fakeViaNetwork(10)
            )
        )
        coEvery { networkDataSource.fetchParcheggio(any()) } returns Either.Right(null)
        val vie = slot<List<ViaEntity>>()
        coEvery { localDataSource.replaceDettagli("arco", capture(vie), null, NOW) } returns Unit

        repository.refreshCragDetails("arco")

        assertEquals(1L, vie.captured.single().settoreId)
    }

    @Test
    fun `refreshCragDetails keeps the saved parking when the map page fails`() = runTest {
        val saved = fakeEntity("arco").copy(parkingLatitude = 44.0, parkingLongitude = 9.0)
        coEvery { localDataSource.getFalesiaWithVieById("arco") } returns FalesiaWithVie(
            saved,
            emptyList(),
            emptyList()
        )
        coEvery { networkDataSource.fetchParcheggio(any()) } returns
                Either.Left(SyncError.NetworkFailure(RuntimeException("timeout")))
        coEvery { localDataSource.replaceDettagli(any(), any(), any(), any()) } returns Unit

        val result = repository.refreshCragDetails("arco")

        assertTrue(result.isRight())
        coVerify { localDataSource.replaceDettagli("arco", emptyList(), 44.0 to 9.0, NOW) }
    }

    @Test
    fun `refreshCragDetails saves nothing when a sector page fails`() = runTest {
        val error = SyncError.ParseFailure("Sector page without route list")
        coEvery { localDataSource.getFalesiaWithVieById("arco") } returns
                fakeWithVie("arco", settori = listOf(fakeSettoreEntity(1, position = 0)))
        coEvery { networkDataSource.fetchVieSettore(any()) } returns Either.Left(error)
        coEvery { networkDataSource.fetchParcheggio(any()) } returns Either.Right(null)

        val result = repository.refreshCragDetails("arco")

        assertEquals(error, result.leftOrNull())
        coVerify(exactly = 0) { localDataSource.replaceDettagli(any(), any(), any(), any()) }
    }

    @Test
    fun `refreshCragDetails returns Left CragNotFound for an unknown crag`() = runTest {
        coEvery { localDataSource.getFalesiaWithVieById("unknown") } returns null

        val result = repository.refreshCragDetails("unknown")

        assertEquals(SyncError.CragNotFound("unknown"), result.leftOrNull())
    }

    @Test
    fun `refreshCragDetails returns Left StorageFailure when the DB write fails`() = runTest {
        coEvery { localDataSource.getFalesiaWithVieById("arco") } returns fakeWithVie("arco")
        coEvery { networkDataSource.fetchParcheggio(any()) } returns Either.Right(null)
        coEvery {
            localDataSource.replaceDettagli(
                any(),
                any(),
                any(),
                any()
            )
        } throws RuntimeException("disk full")

        val result = repository.refreshCragDetails("arco")

        assertTrue(result.leftOrNull() is SyncError.StorageFailure)
    }

    // ── upsertFalesie / deleteAllFalesie / timestamps ─────────────────────

    @Test
    fun `upsertFalesie writes each crag with its own sectors and routes`() = runTest {
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any(), any()) } returns Unit
        val withRoute = fakeDomain("arco").copy(
            settori = listOf(fakeSettore(1)),
            vie = listOf(Via(id = 10, nome = "Spigolo", grado = "6b", settoreId = 1)),
        )

        val result = repository.upsertFalesie(listOf(withRoute, fakeDomain("finale")))

        assertTrue(result.isRight())
        coVerify {
            localDataSource.upsertFalesiaWithVie(
                match { it.id == "arco" },
                match { it.single().id == 1L && it.single().falesiaId == "arco" },
                match { it.single().falesiaId == "arco" && it.single().settoreId == 1L },
            )
        }
        coVerify {
            localDataSource.upsertFalesiaWithVie(
                match { it.id == "finale" },
                emptyList(),
                emptyList()
            )
        }
    }

    @Test
    fun `upsertFalesie returns Left DatabaseError when a write fails`() = runTest {
        coEvery {
            localDataSource.upsertFalesiaWithVie(
                any(),
                any(),
                any()
            )
        } throws RuntimeException("disk full")

        val result = repository.upsertFalesie(listOf(fakeDomain("arco")))

        assertTrue((result as Either.Left).value is FalesiaError.DatabaseError)
    }

    @Test
    fun `deleteAllFalesie returns Left DatabaseError when exception thrown`() = runTest {
        coEvery { localDataSource.deleteAllFalesie() } throws RuntimeException("locked")

        val result = repository.deleteAllFalesie()

        assertTrue((result as Either.Left).value is FalesiaError.DatabaseError)
    }

    @Test
    fun `newest and oldest timestamps come from the local db`() = runTest {
        coEvery { localDataSource.getNewestUpdateTimestamp() } returns 200L
        coEvery { localDataSource.getOldestUpdateTimestamp() } returns 100L

        assertEquals(200L, repository.getNewestUpdateTimestamp())
        assertEquals(100L, repository.getOldestUpdateTimestamp())
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun fakeEntity(id: String) = FalesiaEntity(
        id = id, nome = id.capitalize(), zona = "", pictureUrl = null,
        latitude = null, longitude = null, numeroVie = 10,
        linkDettagli = "https://example.com/$id", lastUpdated = 0L,
    )

    private fun fakeWithVie(id: String, settori: List<SettoreEntity> = emptyList()) =
        FalesiaWithVie(fakeEntity(id), settori, emptyList())

    private fun fakeDomain(id: String) = Falesia(
        id = id, nome = id.capitalize(), zona = "", pictureUrl = null,
        latitude = null, longitude = null, numeroVie = 10,
        linkDettagli = "https://example.com/$id", vie = emptyList(), lastUpdated = 0L,
    )

    private fun fakeListItem(id: String) = FalesiaListItemNetworkModel(
        nome = id.capitalize(),
        pictureUrl = null,
        routeCount = 10,
        detailUrl = "https://www.falesiaonline.it/falesia/$id",
    )

    private fun fakeDetail(settori: List<SettoreNetworkModel> = emptyList()) =
        FalesiaDetailNetworkModel(
            pictureUrl = null, latitude = null, longitude = null, settori = settori,
        )

    private fun fakeSettoreNetwork(id: Long) = SettoreNetworkModel(
        id = id,
        nome = "Settore $id",
        detailUrl = "https://www.falesiaonline.it/settorefoto/$id/s.html",
        routeCount = 5,
        gradoMin = "5a",
        gradoMax = "7a",
        length = "20m",
        approachMinMinutes = 5,
        approachMaxMinutes = 10,
        exposures = emptyList(),
        rating = null,
        ratingCount = 0,
        style = ViaStyle.Sport,
        latitude = null,
        longitude = null,
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
        exposures = "",
        rating = null,
        ratingCount = 0,
        style = "Sport",
        latitude = null,
        longitude = null,
        linkDettagli = "https://www.falesiaonline.it/settorefoto/$id/s.html",
    )

    private fun fakeSettore(id: Long) = Settore(
        id = id,
        nome = "Settore $id",
        numeroVie = 5,
        gradoMin = "5a",
        gradoMax = "7a",
        length = "20m",
        approachMinMinutes = 5,
        approachMaxMinutes = 10,
        exposures = emptyList(),
        rating = null,
        ratingCount = 0,
        style = ViaStyle.Sport,
        location = null,
        linkDettagli = "",
    )

    private fun fakeViaNetwork(id: Long) = ViaNetworkModel(id = id, nome = "Via $id", grado = "6a")

    @Suppress("DEPRECATION")
    private fun String.capitalize() = replaceFirstChar { it.uppercase() }
}
