package com.nuvio.app.features.p2p

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class DesktopTorrServerIsolationTest {
    @Test fun `busy foreign server is neither reused nor shut down`() = runBlocking {
        val foreign = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val shutdownRequests = AtomicInteger()
        foreign.createContext("/echo") { exchange ->
            val bytes = "foreign-server".toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        foreign.createContext("/shutdown") { exchange ->
            shutdownRequests.incrementAndGet()
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
        foreign.start()
        val directory = Files.createTempDirectory("boatflix-foreign-server-").toFile()
        val own = P2pStreamingEngine.TorrServerBinary(foreign.address.port, directory)
        try {
            assertFalse(own.isRunning())
            own.start()
            assertNotEquals("http://127.0.0.1:${foreign.address.port}", own.baseUrl)
            assertNotNull(own.ownedProcessId)
            assertTrue(own.isRunning())
            own.stop()
            assertEquals(0, shutdownRequests.get())
        } finally {
            own.stop()
            foreign.stop(0)
            directory.deleteRecursively()
        }
    }

    @Test fun `concurrent cold starts use one owned process`() = runBlocking {
        val directory = Files.createTempDirectory("boatflix-parallel-start-").toFile()
        val own = P2pStreamingEngine.TorrServerBinary(0, directory)
        try {
            val pids = coroutineScope {
                List(8) { async(Dispatchers.IO) { own.start(); own.ownedProcessId } }.awaitAll()
            }
            assertTrue(pids.all { it != null })
            assertEquals(1, pids.toSet().size)
            assertTrue(own.isRunning())
        } finally {
            own.stop()
            directory.deleteRecursively()
        }
    }

    @Test fun `instances sharing a storage root have independent databases and lifetimes`() = runBlocking {
        val firstDir = Files.createTempDirectory("boatflix-first-server-").toFile()
        val first = P2pStreamingEngine.TorrServerBinary(0, firstDir)
        var second: P2pStreamingEngine.TorrServerBinary? = null
        try {
            first.start()
            val firstPort = first.baseUrl.substringAfterLast(':').toInt()
            val other = P2pStreamingEngine.TorrServerBinary(firstPort, firstDir)
            second = other
            other.start()
            assertNotEquals(first.baseUrl, other.baseUrl)
            assertNotEquals(first.ownedProcessId, other.ownedProcessId)
            first.stop()
            assertTrue(other.isRunning())
        } finally {
            first.stop()
            second?.stop()
            firstDir.deleteRecursively()
        }
    }

    @Test fun `legacy database lock cannot block a new server`() = runBlocking {
        val directory = Files.createTempDirectory("boatflix-legacy-db-").toFile()
        val own = P2pStreamingEngine.TorrServerBinary(0, directory)
        try {
            java.io.RandomAccessFile(directory.resolve("config.db"), "rw").use { legacy ->
                legacy.channel.lock().use {
                    own.start()
                    assertTrue(own.isRunning())
                    own.stop()
                }
            }
        } finally {
            own.stop()
            directory.deleteRecursively()
        }
    }

    @Test fun `selects another loopback port when preferred port is occupied`() {
        ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1")).use { occupied ->
            assertNotEquals(occupied.localPort, availableLoopbackPort(occupied.localPort))
        }
    }
}
