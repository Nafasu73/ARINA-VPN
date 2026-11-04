package net.ariana.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.graphics.Color
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import net.ariana.vpn.model.ConfigParser

class MainActivity : ComponentActivity() {

    private var activeConfig = ""
    private lateinit var configInput: EditText
    private lateinit var statusText: TextView

    private val gold = Color.rgb(212, 175, 55)
    private val purple = Color.rgb(65, 48, 85)
    private val background = Color.rgb(20, 17, 32)

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
            setPadding(24, 35, 24, 25)
            setBackgroundColor(background)
        }

        val logo = TextView(this).apply {
            text = "Λ"
            textSize = 65f
            gravity = Gravity.CENTER
            setTextColor(gold)
            typeface = Typeface.DEFAULT_BOLD
        }

        val title = TextView(this).apply {
            text = "ARIANA VPN"
            textSize = 29f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "اتصال امن با سرور VPN"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }

        statusText = TextView(this).apply {
            text = "وضعیت: قطع"
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(gold)
            setPadding(0, 28, 0, 24)
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
            isAllCaps = false
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(background)
            backgroundTintList = ColorStateList.valueOf(gold)
            setOnClickListener {
                connectVpn(configInput.text.toString().trim())
            }
        }

        val disconnectButton = Button(this).apply {
            text = "قطع اتصال"
            isAllCaps = false
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(purple)
            setOnClickListener {
                disconnectVpn()
            }
        }

        val footer = TextView(this).apply {
            text = "طراحی و توسعه: فاطمه زهرا ساعی"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 30, 0, 0)
        }

        root.addView(logo)
        root.addView(title)
        root.addView(subtitle)
        root.addView(statusText)

        root.addView(configInput, LinearLayout.LayoutParams(-1, 150))

        root.addView(connectButton, LinearLayout.LayoutParams(
            -1, 58
        ).apply { topMargin = 22 })

        root.addView(disconnectButton, LinearLayout.LayoutParams(
            -1, 58
        ).apply { topMargin = 12 })

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
        statusText.text = "درخواست اتصال ارسال شد؛ در انتظار پاسخ سرور"
    }

    private fun disconnectVpn() {
        val serviceIntent = Intent(this, ArianaVpnService::class.java).apply {
            action = ArianaVpnService.ACTION_DISCONNECT
        }

        startService(serviceIntent)
        statusText.text = "وضعیت: قطع"
    }
}
