package net.ariana.vpn

import com.wireguard.android.backend.Tunnel

class WireGuardTunnel : Tunnel {
    override fun getName(): String = "ARIANA"

    override fun onStateChange(newState: Tunnel.State) {
        android.util.Log.i(
            "ARIANA",
            "WireGuard state: $newState"
        )
    }
}
