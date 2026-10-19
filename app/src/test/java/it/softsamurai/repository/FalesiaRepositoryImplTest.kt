package it.softsamurai.repository

import app.cash.turbine.test
import arrow.core.Either
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.softsamurai.repository.database.entity.ViaEntity
import it.softsamurai.domain.error.FalesiaError
import it.softsamurai.domain.error.SyncError
import it.softsamurai.domain.model.Falesia
import it.softsamurai.domain.model.Via
import it.softsamurai.repository.database.entity.FalesiaEntity
import it.softsamurai.repository.database.relation.FalesiaWithVie
import it.softsamurai.repository.local.FalesiaLocalDataSource
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
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
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } returns Unit

        val result = repository.upsertFalesia(falesia)

        assertTrue(result.isRight())
        coVerify { localDataSource.upsertFalesiaWithVie(any(), any()) }
    }

    @Test
    fun `upsertFalesia returns Left DatabaseError when exception thrown`() = runTest {
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } throws RuntimeException("db fail")

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
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } returns Unit

        val result = repository.syncFalesie()

        assertTrue(result.isRight())
        coVerify(exactly = 2) { localDataSource.upsertFalesiaWithVie(any(), any()) }
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
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } throws RuntimeException("disk full")

        val result = repository.syncFalesie()

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is SyncError.StorageFailure)
    }

    @Test
    fun `syncFalesie stores every crag with id from url, mapped routes and the sync time`() = runTest {
        coEvery { networkDataSource.fetchElencoFalesie() } returns
            Either.Right(listOf(fakeListItem("arco"), fakeListItem("finale")))
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns
            Either.Right(fakeDetail(vie = listOf(ViaNetworkModel(nome = "Spigolo", grado = "6b"))))
        val saved = mutableListOf<Pair<FalesiaEntity, List<ViaEntity>>>()
        val entity = slot<FalesiaEntity>()
        val vie = slot<List<ViaEntity>>()
        coEvery { localDataSource.upsertFalesiaWithVie(capture(entity), capture(vie)) } answers {
            saved += entity.captured to vie.captured
        }

        repository.syncFalesie()

        assertEquals(listOf("arco", "finale"), saved.map { it.first.id })
        assertTrue(saved.all { it.first.lastUpdated == NOW })
        assertEquals(listOf("arco", "finale"), saved.map { it.second.single().falesiaId })
        assertEquals("6b", saved.first().second.single().grado)
    }

    @Test
    fun `syncFalesie fetches the details of every crag beyond the concurrency chunk`() = runTest {
        val ids = (1..12).map { "crag$it" }
        coEvery { networkDataSource.fetchElencoFalesie() } returns Either.Right(ids.map { fakeListItem(it) })
        coEvery { networkDataSource.fetchDettagliFalesia(any()) } returns Either.Right(fakeDetail())
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } returns Unit

        val result = repository.syncFalesie()

        assertTrue(result.isRight())
        ids.forEach { id ->
            coVerify(exactly = 1) { networkDataSource.fetchDettagliFalesia("https://www.falesiaonline.it/falesia/$id") }
        }
        coVerify(exactly = 12) { localDataSource.upsertFalesiaWithVie(any(), any()) }
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
        coVerify(exactly = 0) { localDataSource.upsertFalesiaWithVie(any(), any()) }
    }

    // ── upsertFalesie / deleteAllFalesie / timestamps ─────────────────────

    @Test
    fun `upsertFalesie writes each crag with its own routes`() = runTest {
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } returns Unit
        val withRoute = fakeDomain("arco").copy(vie = listOf(Via(nome = "Spigolo", grado = "6b")))

        val result = repository.upsertFalesie(listOf(withRoute, fakeDomain("finale")))

        assertTrue(result.isRight())
        coVerify { localDataSource.upsertFalesiaWithVie(match { it.id == "arco" }, match { it.single().falesiaId == "arco" }) }
        coVerify { localDataSource.upsertFalesiaWithVie(match { it.id == "finale" }, emptyList()) }
    }

    @Test
    fun `upsertFalesie returns Left DatabaseError when a write fails`() = runTest {
        coEvery { localDataSource.upsertFalesiaWithVie(any(), any()) } throws RuntimeException("disk full")

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

    private fun fakeWithVie(id: String) = FalesiaWithVie(fakeEntity(id), emptyList())

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

    private fun fakeDetail(vie: List<ViaNetworkModel> = emptyList()) = FalesiaDetailNetworkModel(
        pictureUrl = null, latitude = null, longitude = null, vie = vie,
    )

    @Suppress("DEPRECATION")
    private fun String.capitalize() = replaceFirstChar { it.uppercase() }
}
