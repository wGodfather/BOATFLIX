package com.nuvio.app.features.streams

import kotlinx.coroutines.*
import kotlin.test.*

class TorrentListingLatencyTest {
    private val context = StreamVerificationContext("movie", "tt1234567", listOf("Example Movie"), 2026)
    private fun candidate() = StreamItem(
        name = "Example Movie 2026 1080p [2 GB] [👤 20]", addonName = "B.O.A.T", addonId = "boat",
        infoHash = "0123456789012345678901234567890123456789", seeders = 20,
        behaviorHints = StreamBehaviorHints(videoSize = 2_147_483_648L),
    )

    @Test fun `complete torrent rows publish while metadata-less torrent remains stalled`() = runBlocking {
        val complete = candidate()
        val unknown = candidate().copy(name = "Example Movie 2026", behaviorHints = StreamBehaviorHints())
        val rows = mutableListOf<StreamItem>()
        val completePublished = CompletableDeferred<Unit>()
        val stalled = CompletableDeferred<Unit>()
        val job = launch {
            publishEligibleStreams(listOf(unknown) + List(20) { complete }, verify = { stream ->
                prepareStreamForListing(stream, context) { _, _ -> stalled.complete(Unit); awaitCancellation() }
            }, publish = { rows += it; if (rows.size == 20) completePublished.complete(Unit) })
        }
        try {
            withTimeout(2000) { stalled.await(); completePublished.await() }
            assertEquals(20, rows.size)
            assertTrue(rows.all { it.verifiedMedia == null })
            assertTrue(rows.all { StreamListingPolicy.isVisible(it) })
        } finally { job.cancelAndJoin() }
    }

    @Test fun `missing size or resolution keeps media measurement fallback`() = runBlocking {
        for (stream in listOf(candidate().copy(behaviorHints = StreamBehaviorHints(), name = "Example Movie 2026 1080p"),
            candidate().copy(name = "Example Movie 2026"))) {
            var measured = false
            prepareStreamForListing(stream, context) { _, _ -> measured = true; null }
            assertTrue(measured)
        }
    }

    @Test fun `other titles years and season packs cannot bypass content matching`() {
        for (name in listOf("Other Movie 2026 1080p [2 GB]", "Example Movie 2025 1080p [2 GB]",
            "Example Movie 2026 Collection 1080p [2 GB]")) {
            assertFalse(candidate().copy(name = name).hasCompleteTorrentListingMetadata(context))
        }
        val series = context.copy(type = "series", season = 1, episode = 2)
        assertTrue(candidate().copy(name = "Example Movie S01E02 1080p").hasCompleteTorrentListingMetadata(series))
        assertFalse(candidate().copy(name = "Example Movie S01E03 1080p").hasCompleteTorrentListingMetadata(series))
        assertFalse(candidate().copy(name = "Example Movie Season 1 Complete 1080p").hasCompleteTorrentListingMetadata(series))
    }
}
