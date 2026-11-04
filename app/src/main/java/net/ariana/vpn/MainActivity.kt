package net.ariana.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import net.ariana.vpn.model.ConfigParser

class MainActivity : ComponentActivity() {

    private var activeConfig = ""
    private lateinit var configInput: EditText
    private lateinit var statusText: TextView

    private val vpnPrepareLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                startVpnServiceInternal()
            } else {
                statusText.text = "مجوز VPN داده نشد"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createScreen()

        intent?.data?.toString()?.let {
            configInput.setText(it)
        }
    }

    private fun createScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 35, 28, 25)
            setBackgroundColor(Color.rgb(20, 17, 32))
        }

        val logo = TextView(this).apply {
            text = "Λ"
            textSize = 65f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(212, 175, 55))
        }

        val title = TextView(this).apply {
            text = "ARIANA VPN"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "اتصال امن با سرور VPN"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }

        statusText = TextView(this).apply {
            text = "وضعیت: قطع"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(230, 190, 65))
            setPadding(0, 35, 0, 25)
        }

        configInput = EditText(this).apply {
            hint = "کانفیگ VLESS یا WireGuard را وارد کنید"
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            gravity = Gravity.TOP or Gravity.RIGHT
            minLines = 4
            setPadding(15, 15, 15, 15)
            setBackgroundColor(Color.rgb(48, 43, 60))
        }

        val connectButton = Button(this).apply {
            text = "اتصال به VPN"
            setOnClickListener {
                connectVpn(configInput.text.toString().trim())
            }
        }

        val disconnectButton = Button(this).apply {
            text = "قطع اتصال"
            setOnClickListener {
                disconnectVpn()
            }
        }

        val footer = TextView(this).apply {
            text = "طراحی و توسعه: فاطمه زهرا ساعی"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 35, 0, 0)
        }

        root.addView(logo)
        root.addView(title)
        root.addView(subtitle)
        root.addView(statusText)

        root.addView(configInput, LinearLayout.LayoutParams(
            -1, 150
        ))

        root.addView(connectButton, LinearLayout.LayoutParams(
            -1, 58
        ).apply { topMargin = 25 })

        root.addView(disconnectButton, LinearLayout.LayoutParams(
            -1, 58
        ))

        root.addView(footer)

        setContentView(root)
    }

    private fun connectVpn(config: String) {
        if (config.isBlank()) {
            Toast.makeText(this, "ابتدا کانفیگ را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val vless = ConfigParser.parseVless(config)
        val wg = ConfigParser.parseWireGuard(config)

        if (vless == null && wg == null) {
            Toast.makeText(this, "کانفیگ معتبر نیست", Toast.LENGTH_SHORT).show()
            return
        }

        activeConfig = config

        val prepareIntent = VpnService.prepare(this)

        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            startVpnServiceInternal()
        }
    }

    private fun startVpnServiceInternal() {
        val protocol =
            if (activeConfig.startsWith("vless://")) "VLESS" else "WireGuard"

        val serviceIntent = Intent(this, ArianaVpnService::class.java).apply {
            action = ArianaVpnService.ACTION_CONNECT
            putExtra(ArianaVpnService.EXTRA_CONFIG, activeConfig)
            putExtra(ArianaVpnService.EXTRA_PROTOCOL, protocol)
        }

        startService(serviceIntent)
        statusText.text = "درخواست اتصال ارسال شد؛ در انتظار بررسی وضعیت سرور"
    }

    private fun disconnectVpn() {
        val serviceIntent = Intent(this, ArianaVpnService::class.java).apply {
            action = ArianaVpnService.ACTION_DISCONNECT
        }

        startService(serviceIntent)
        statusText.text = "وضعیت: قطع"
    }
}
