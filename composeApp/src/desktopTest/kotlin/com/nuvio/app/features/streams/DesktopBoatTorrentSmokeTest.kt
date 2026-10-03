package com.nuvio.app.features.streams

import com.nuvio.app.features.plugins.PluginScraper
import com.nuvio.app.features.plugins.runtime.PluginRuntime
import kotlinx.coroutines.*
import org.junit.Assume.assumeTrue
import java.io.File
import kotlin.test.*

/** Explicitly enabled live check; normal CI does not depend on public source services. */
class DesktopBoatTorrentSmokeTest {
    @Test fun `boat candidates publish promptly even when other torrent checks are blocked`() = runBlocking {
        val providerFile = System.getenv("BOATFLIX_SMOKE_PROVIDER_FILE")
        assumeTrue("Set BOATFLIX_SMOKE_PROVIDER_FILE to run the live source check", !providerFile.isNullOrBlank())
        val started = System.nanoTime()
        val code = File(providerFile!!).readText()
        val results = PluginRuntime.executePlugin(code, "tt22084616", "movie", null, null, "boat-listing-smoke", false)
        val scraper = PluginScraper("boat-listing-smoke", "local-smoke", "B.O.A.T", "", "smoke", "boat.js",
            listOf("movie"), true, true, code = code)
        val context = StreamSourceVerifier.prepareContext(StreamVerificationContext("movie", "tt22084616",
            listOf("Spider-Man: Brand New Day", "Örümcek-Adam: Yepyeni Bir Gün"), year = 2026))
        val streams = results.map { it.toStreamItem(scraper) }.sortedBySizeAndQuality()
        val expected = streams.count { it.hasCompleteTorrentListingMetadata(context) }
        assertTrue(expected >= 8, "Expected at least the eight BOAT candidates shown by Nuvio")
        val afterProvider = System.nanoTime()
        val published = mutableListOf<StreamItem>()
        val ready = CompletableDeferred<Unit>()
        val job = launch {
            publishEligibleStreams(streams, verify = { stream ->
                prepareStreamForListing(stream, context) { _, _ -> awaitCancellation() }
            }, publish = { published += it; if (published.size == expected) ready.complete(Unit) })
        }
        try {
            withTimeout(2000) { ready.await() }
            println("BOAT_LISTING providerAndContextMs=${(afterProvider-started)/1_000_000} publicationMs=${(System.nanoTime()-afterProvider)/1_000_000} results=${results.size} visibleTorrents=${published.size}")
            assertTrue(published.all { it.verifiedMedia == null })
        } finally { job.cancelAndJoin() }
    }

    @Test fun `boat torrent survives the Windows source listing pipeline`() = runBlocking {
        val providerFile = System.getenv("BOATFLIX_SMOKE_PROVIDER_FILE")
        assumeTrue("Set BOATFLIX_SMOKE_PROVIDER_FILE to run the live source check", !providerFile.isNullOrBlank())
        val code = File(providerFile!!).readText()
        val videoId = System.getenv("BOATFLIX_SMOKE_IMDB") ?: "tt22084616"
        val results = PluginRuntime.executePlugin(code, videoId, "movie", null, null, "boat-live-smoke", false)
        val scraper = PluginScraper("boat-live-smoke", "local-smoke", "B.O.A.T", "", "smoke", "boat.js",
            listOf("movie"), true, true, code = code)
        val candidates = results.map { it.toStreamItem(scraper) }
            .filter { it.isTorrentStream && StreamListingPolicy.beforeVerification(it) }
            .sortedByDescending { StreamListingPolicy.seedCount(it) ?: 0 }.take(4)
        println("BOAT_SMOKE results=${results.size} eligibleTorrentCandidates=${candidates.size}")
        assertTrue(candidates.isNotEmpty(), "B.O.A.T returned no eligible torrent candidates")
        val context = StreamSourceVerifier.prepareContext(StreamVerificationContext("movie", videoId,
            listOf("Spider-Man: Brand New Day", "Örümcek-Adam: Yepyeni Bir Gün"), year = 2026))
        val verified = withTimeout(180_000L) {
            coroutineScope {
                candidates.map { stream -> async {
                    val checked = StreamSourceVerifier.verify(stream, context)
                    println("BOAT_SMOKE seeders=${StreamListingPolicy.seedCount(stream)} verified=${checked != null} visible=${checked?.let(StreamListingPolicy::isVisible)}")
                    checked?.takeIf(StreamListingPolicy::isVisible)
                } }.awaitAll().filterNotNull()
            }
        }
        println("BOAT_SMOKE verifiedVisibleTorrents=${verified.size}")
        assertTrue(verified.isNotEmpty(), "No torrent survived Windows verification; inspect source-verification.log")
    }
}
