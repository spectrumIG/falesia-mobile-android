package it.softsamurai.repository.fixture

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import it.softsamurai.core.di.IoDispatcher
import it.softsamurai.domain.error.SyncError
import it.softsamurai.repository.FalesiaRepositoryImpl
import it.softsamurai.repository.local.FalesiaLocalDataSource
import it.softsamurai.repository.mapper.idFromUrl
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/** Sample data counts as never synced with the site, so the first manual download is never skipped. */
private const val SAMPLE_DATA_TIME = 0L

/**
 * Fills an empty DB with the saved pages (debug builds, see `DownloadMode.Manual`): the crag list, then every
 * crag's routes and parking, through the same repository code as the real sync.
 */
class FixtureDataLoader @Inject constructor(
    private val localDataSource: FalesiaLocalDataSource,
    private val fixtures: FixtureNetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun loadIfEmpty(): Either<SyncError, Unit> {
        if (localDataSource.countFalesie() > 0) return Unit.right()
        val repository = FalesiaRepositoryImpl(localDataSource, fixtures, ioDispatcher) { SAMPLE_DATA_TIME }
        return either {
            repository.syncFalesie().bind()
            fixtures.fetchElencoFalesie().bind().forEach { crag ->
                repository.refreshCragDetails(crag.idFromUrl()).bind()
            }
        }
    }
}
