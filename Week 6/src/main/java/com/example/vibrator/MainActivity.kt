package com.example.vibrator // Make sure this package name matches your project

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var vibrator: Vibrator
    private lateinit var toggleButton: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Corrected to use the XML file name

        // Get the system's vibrator service
        vibrator = getSystemService(Vibrator::class.java)

        toggleButton = findViewById(R.id.toggleButtonVibrate)

        // Set a listener to react when the button is toggled
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Button is ON
                // Create a vibration pattern: Vibrate for 400ms, pause for 600ms, repeat
                val pattern = longArrayOf(0, 400, 600)

                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                // Button is OFF
                // Cancel any ongoing vibration
                vibrator.cancel()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Good practice: Stop vibrating if the app is paused (e.g., user hits home button)
        vibrator.cancel()
        toggleButton.isChecked = false
    }
}