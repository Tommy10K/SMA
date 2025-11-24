package com.example.nativeapis
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nativeapis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var telephonyManager: TelephonyManager

    private val requiredPermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Launcher modern pentru permisiuni
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            log("Permisiuni acordate! Poți folosi butoanele.")
        } else {
            log("EROARE: Permisiunile sunt obligatorii pentru demo.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        setupListeners()
        checkAndRequestPermissions()
    }

    private fun setupListeners() {
        binding.btnCall.setOnClickListener {
            val number = binding.etPhoneNumber.text.toString()
            if (number.isNotEmpty()) makePhoneCall(number)
            else toast("Introdu un număr!")
        }

        binding.btnSms.setOnClickListener {
            val number = binding.etPhoneNumber.text.toString()
            if (number.isNotEmpty()) sendSms(number)
            else toast("Introdu un număr!")
        }

        binding.btnCheckSignal.setOnClickListener {
            readSignalStrength()
        }
    }

    private fun makePhoneCall(number: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$number")
                startActivity(intent)
                log("Apel inițiat către $number")
            } catch (e: Exception) {
                log("Eroare la apel: ${e.message}")
            }
        } else {
            log("Nu am permisiunea CALL_PHONE")
        }
    }

    private fun sendSms(number: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    this.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                smsManager.sendTextMessage(number, null, "Salut din Android Emulator!", null, null)
                log("SMS trimis către $number")
            } catch (e: Exception) {
                log("Eroare SMS: ${e.message}")
            }
        } else {
            log("Nu am permisiunea SEND_SMS")
        }
    }

    private fun readSignalStrength() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            log("Nu am permisiunea LOCATION (necesară pt semnal)")
            return
        }

        log("Se citește semnalul...")

        val cellInfos = telephonyManager.allCellInfo
        if (cellInfos.isNullOrEmpty()) {
            log("Nu s-au găsit informații despre celule (Emulator?). Verifică Extended Controls.")
            return
        }

        for (cellInfo in cellInfos) {
            if (cellInfo is CellInfoLte) {
                val signal = cellInfo.cellSignalStrength
                log("LTE Signal: ${signal.dbm} dBm (Level: ${signal.level}/4)")
            } else if (cellInfo is CellInfoGsm) {
                val signal = cellInfo.cellSignalStrength
                log("GSM Signal: ${signal.dbm} dBm (Level: ${signal.level}/4)")
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions)
        }
    }

    private fun log(message: String) {
        binding.tvLogs.append("\n> $message")
        // Scroll automat jos
        val scrollAmount = binding.tvLogs.layout?.getLineTop(binding.tvLogs.lineCount) ?: 0
        if (scrollAmount > binding.tvLogs.height) {
            binding.tvLogs.scrollTo(0, scrollAmount - binding.tvLogs.height)
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}