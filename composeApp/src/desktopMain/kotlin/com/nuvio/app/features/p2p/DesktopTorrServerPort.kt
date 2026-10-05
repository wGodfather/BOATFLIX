package com.nuvio.app.features.p2p

import java.net.InetSocketAddress
import java.net.ServerSocket

/** The preferred BOATFLIX port is separate from Nuvio's 8091. */
internal fun availableLoopbackPort(preferredPort: Int): Int {
    fun bind(port: Int): Int = ServerSocket().use { socket ->
        socket.reuseAddress = false
        socket.bind(InetSocketAddress("127.0.0.1", port))
        socket.localPort
    }
    return try { bind(preferredPort) } catch (_: java.io.IOException) { bind(0) }
}
