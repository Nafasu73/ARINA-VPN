package net.ariana.vpn.model

import java.net.URI
import java.net.URLDecoder

/**
 * Protocol Models and Parser for VLESS Reality and WireGuard
 * Developed by Fatemeh Zahra Saei (فاطمه زهرا ساعی)
 */
sealed class VpnConfig(
    open val name: String,
    open val serverAddress: String,
    open val port: Int
)

data class VlessConfig(
    override val name: String,
    override val serverAddress: String,
    override val port: Int,
    val uuid: String,
    val flow: String,
    val security: String,
    val sni: String,
    val publicKey: String,
    val shortId: String,
    val fingerprint: String
) : VpnConfig(name, serverAddress, port)

data class WireGuardConfig(
    override val name: String,
    override val serverAddress: String,
    override val port: Int,
    val clientPrivateKey: String,
    val clientAddress: String,
    val serverPublicKey: String,
    val allowedIps: String,
    val dns: String,
    val mtu: Int = 1420
) : VpnConfig(name, serverAddress, port)

object ConfigParser {

    /**
     * Parses standard vless:// URI
     * Format: vless://UUID@HOST:PORT?encryption=none&flow=xtls-rprx-vision&security=reality&sni=...#Name
     */
    fun parseVless(uriString: String): VlessConfig? {
        return try {
            val trimmed = uriString.trim()
            if (!trimmed.startsWith("vless://")) return null

            val uri = URI(trimmed)
            val uuid = uri.userInfo ?: return null
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443

            val queryParams = uri.rawQuery?.split("&")?.associate {
                val parts = it.split("=")
                val key = parts.getOrNull(0) ?: ""
                val value = parts.getOrNull(1) ?: ""
                key to URLDecoder.decode(value, "UTF-8")
            } ?: emptyMap()

            val name = if (uri.fragment != null) URLDecoder.decode(uri.fragment, "UTF-8") else "ARIANA-VLESS"

            VlessConfig(
                name = name,
                serverAddress = host,
                port = port,
                uuid = uuid,
                flow = queryParams["flow"] ?: "xtls-rprx-vision",
                security = queryParams["security"] ?: "reality",
                sni = queryParams["sni"] ?: host,
                publicKey = queryParams["pbk"] ?: "",
                shortId = queryParams["sid"] ?: "",
                fingerprint = queryParams["fp"] ?: "chrome"
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses WireGuard configuration string or URI
     * Supports standard INI format [Interface] and [Peer] or wireguard:// URI
     */
    fun parseWireGuard(content: String): WireGuardConfig? {
        return try {
            val lines = content.lines().map { it.trim() }
            var privateKey = ""
            var address = "10.0.0.2/24"
            var publicKey = ""
            var endpoint = ""
            var allowedIps = "0.0.0.0/0, ::/0"
            var dns = "1.1.1.1"

            for (line in lines) {
                if (line.startsWith("#") || line.isEmpty()) continue
                val parts = line.split("=", limit = 2)
                if (parts.size != 2) continue
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim()

                when (key) {
                    "privatekey" -> privateKey = value
                    "address" -> address = value
                    "publickey" -> publicKey = value
                    "endpoint" -> endpoint = value
                    "allowedips" -> allowedIps = value
                    "dns" -> dns = value
                }
            }

            if (endpoint.isNotEmpty() && privateKey.isNotEmpty() && publicKey.isNotEmpty()) {
                val hostParts = endpoint.split(":")
                val host = hostParts[0].trim()
                val port = hostParts.getOrNull(1)?.toIntOrNull() ?: 51820

                WireGuardConfig(
                    name = "ARIANA-WireGuard",
                    serverAddress = host,
                    port = port,
                    clientPrivateKey = privateKey,
                    clientAddress = address,
                    serverPublicKey = publicKey,
                    allowedIps = allowedIps,
                    dns = dns
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
