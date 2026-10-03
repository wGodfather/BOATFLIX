package com.nuvio.app.features.plugins

import com.nuvio.app.features.plugins.runtime.PluginRuntime
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class PluginFetchTimeoutTest {
    @Test fun `slow secondary fetch cannot hold back ready streams until global plugin timeout`() = runBlocking {
        withServer { server, url ->
            server.createContext("/fast") { exchange ->
                val bytes = "[]".toByteArray()
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.createContext("/slow") { exchange ->
                try { Thread.sleep(5000); exchange.sendResponseHeaders(200, -1) }
                catch (_: Exception) { } finally { exchange.close() }
            }
            val started = System.nanoTime()
            val results = withTimeout(2500) {
                PluginRuntime.executePlugin("""
                    module.exports.getStreams = async function() {
                        var results = await Promise.allSettled([
                            fetch('$url/fast').then(function() { return [{title:'ready', url:'https://example.test/ready.mp4'}]; }),
                            fetch('$url/slow', {signal: AbortSignal.timeout(200)}).then(function() { return []; })
                        ]);
                        return results[0].status === 'fulfilled' ? results[0].value : [];
                    };
                """.trimIndent(), "1", "movie", null, null, "timeout-test", false)
            }
            assertEquals("ready", results.single().title)
            assertTrue((System.nanoTime() - started) / 1_000_000 < 2500)
        }
    }

    @Test fun `already aborted fetch never reaches the server`() = runBlocking {
        withServer { server, url ->
            val requests = AtomicInteger()
            server.createContext("/") { requests.incrementAndGet(); it.sendResponseHeaders(200, -1); it.close() }
            val results = PluginRuntime.executePlugin("""
                module.exports.getStreams = async function() {
                    var controller = new AbortController(); controller.abort();
                    try { await fetch('$url/', {signal:controller.signal}); return []; }
                    catch(error) { return [{title:error.name, url:'https://example.test/aborted.mp4'}]; }
                };
            """.trimIndent(), "1", "movie", null, null, "aborted-test", false)
            assertEquals("AbortError", results.single().title)
            assertEquals(0, requests.get())
        }
    }

    private suspend fun withServer(block: suspend (HttpServer, String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val executor = Executors.newCachedThreadPool()
        server.executor = executor
        server.start()
        try { block(server, "http://127.0.0.1:${server.address.port}") }
        finally { server.stop(0); executor.shutdownNow() }
    }
}
