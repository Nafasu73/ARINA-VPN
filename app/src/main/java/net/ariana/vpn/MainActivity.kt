package net.ariana.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import net.ariana.vpn.model.ConfigParser

/**
 * ARIANA VPN - Android Main Entry Activity
 * Developed by Fatemeh Zahra Saei (فاطمه زهرا ساعی)
 * 100% Free - Zero Fees - Open Access
 */
class MainActivity : ComponentActivity() {

    private var isConnected = false
    private var activeConfig: String = ""

    // VpnService permission launcher
    private val vpnPrepareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnServiceInternal()
        } else {
            Toast.makeText(this, "مجوز ایجاد تونل VPN توسط کاربر لغو شد", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle incoming intent with custom configuration link (vless:// or wireguard://)
        val data = intent?.data
        if (data != null) {
            importConfig(data.toString())
        }
    }

    /**
     * User clicks connect button with either selected server or custom imported config
     */
    fun connectVpn(configString: String) {
        activeConfig = configString

        val vless = ConfigParser.parseVless(configString)
        val wg = ConfigParser.parseWireGuard(configString)

        if (vless == null && wg == null) {
            Toast.makeText(this, "ساختار کانفیگ VLESS یا WireGuard نامعتبر است", Toast.LENGTH_SHORT).show()
            return
        }

        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            startVpnServiceInternal()
        }
    }

    private fun startVpnServiceInternal() {
        val protocol = if (activeConfig.startsWith("vless://")) "VLESS Reality" else "WireGuard"
        val intent = Intent(this, ArianaVpnService::class.java).apply {
            action = ArianaVpnService.ACTION_CONNECT
            putExtra(ArianaVpnService.EXTRA_CONFIG, activeConfig)
            putExtra(ArianaVpnService.EXTRA_PROTOCOL, protocol)
        }
        startService(intent)
        isConnected = true
        Toast.makeText(this, "آریانا وی‌پی‌ان با موفقیت فعال شد", Toast.LENGTH_SHORT).show()
    }

    fun disconnectVpn() {
        val intent = Intent(this, ArianaVpnService::class.java).apply {
            action = ArianaVpnService.ACTION_DISCONNECT
        }
        startService(intent)
        isConnected = false
        Toast.makeText(this, "اتصال وی‌پی‌ان قطع گردید", Toast.LENGTH_SHORT).show()
    }

    fun importConfig(text: String) {
        val vless = ConfigParser.parseVless(text)
        val wg = ConfigParser.parseWireGuard(text)

        if (vless != null) {
            activeConfig = text
            Toast.makeText(this, "کانفیگ VLESS سرور ${vless.name} با موفقیت دریافت شد", Toast.LENGTH_SHORT).show()
        } else if (wg != null) {
            activeConfig = text
            Toast.makeText(this, "کانفیگ WireGuard با موفقیت دریافت شد", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "متن ورودی شامل لینک معتبر VLESS یا WireGuard نیست", Toast.LENGTH_SHORT).show()
        }
    }
}
