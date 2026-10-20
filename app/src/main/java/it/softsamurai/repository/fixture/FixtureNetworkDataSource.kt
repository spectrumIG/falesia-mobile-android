package it.softsamurai.repository.fixture

import android.content.Context
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dagger.hilt.android.qualifiers.ApplicationContext
import it.softsamurai.domain.error.SyncError
import it.softsamurai.repository.network.FalesiaNetworkDataSource
import it.softsamurai.repository.network.model.FalesiaDetailNetworkModel
import it.softsamurai.repository.network.model.FalesiaListItemNetworkModel
import it.softsamurai.repository.network.model.ViaNetworkModel
import it.softsamurai.repository.network.parseDettagliFalesia
import it.softsamurai.repository.network.parseElencoFalesie
import it.softsamurai.repository.network.parseParcheggio
import it.softsamurai.repository.network.parseVieSettore
import java.io.IOException
import javax.inject.Inject

private const val ROOT = "fixtures"
private val SECTOR_ID = Regex("""/settore[a-z_]*/(\d+)/""")

/** Reads a saved page by its path under `fixtures/`, null when it is not there. */
fun interface FixtureReader {
    fun read(path: String): String?
}

/** Saved falesiaonline.it pages shipped as debug assets (`app/src/debug/assets/fixtures/`). */
class AssetFixtureReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : FixtureReader {
    override fun read(path: String): String? = try {
        context.assets.open("$ROOT/$path").bufferedReader().use { it.readText() }
    } catch (_: IOException) {
        null
    }
}

/**
 * The scraper's data source on saved pages instead of the site, for debug builds (see `DownloadMode.Manual`):
 * the same parsers run, so the sample data goes through the real pipeline.
 *
 * Layout: `elenco.html` (crag list), `falesia/<slug>.html`, `map/<slug>.html`, `settore/<id>.html`. The list
 * keeps only the crags whose page is saved; a sector without a saved page has no routes.
 */
class FixtureNetworkDataSource @Inject constructor(
    private val reader: FixtureReader,
) : FalesiaNetworkDataSource {

    override suspend fun fetchElencoFalesie(): Either<SyncError, List<FalesiaListItemNetworkModel>> =
        page("elenco.html").map { document ->
            parseElencoFalesie(document).filter { reader.read("falesia/${slug(it.detailUrl)}.html") != null }
        }

    override suspend fun fetchDettagliFalesia(url: String): Either<SyncError, FalesiaDetailNetworkModel> =
        page("falesia/${slug(url)}.html").flatMap(::parseDettagliFalesia)

    override suspend fun fetchVieSettore(url: String): Either<SyncError, List<ViaNetworkModel>> {
        val id = SECTOR_ID.find(url)?.groupValues?.get(1)
        val html = id?.let { reader.read("settore/$it.html") } ?: return emptyList<ViaNetworkModel>().right()
        return parseVieSettore(Ksoup.parse(html))
    }

    override suspend fun fetchParcheggio(detailUrl: String): Either<SyncError, Pair<Double, Double>?> =
        reader.read("map/${slug(detailUrl)}.html")?.let { parseParcheggio(Ksoup.parse(it)) }.right()

    private fun page(path: String): Either<SyncError, Document> =
        reader.read(path)?.let { Ksoup.parse(it).right() }
            ?: Either.Left(SyncError.ParseFailure("Missing fixture $path"))

    private fun slug(url: String): String = url.trimEnd('/').substringAfterLast('/').removeSuffix(".html")
}
