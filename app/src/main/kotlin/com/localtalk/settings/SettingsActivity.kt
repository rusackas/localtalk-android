package com.localtalk.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.localtalk.R
import com.localtalk.transcription.WhisperTranscriber
import java.io.File

// This activity is the launcher entry point. Its job is to walk the user
// through the three setup steps: grant mic permission, enable the keyboard,
// and download the Whisper model.
class SettingsActivity : AppCompatActivity() {

    private val requestMic = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updateUI() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 80, 64, 80)
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 28f
        }

        val stepMic = Button(this).apply {
            text = "1. Grant Microphone Permission"
            setOnClickListener {
                requestMic.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        val stepKeyboard = Button(this).apply {
            text = "2. Enable LocalTalk Keyboard"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val stepModel = Button(this).apply {
            text = "3. Download Whisper Model (~150 MB)"
            setOnClickListener { downloadModel() }
        }

        val switchKeyboard = Button(this).apply {
            text = "Switch to LocalTalk Keyboard"
            setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                @Suppress("DEPRECATION")
                imm.showInputMethodPicker()
            }
        }

        layout.addView(title)
        layout.addView(stepMic)
        layout.addView(stepKeyboard)
        layout.addView(stepModel)
        layout.addView(switchKeyboard)

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        // Visual feedback (checkmarks, etc.) can be wired up here as the UI matures.
        @Suppress("UNUSED_VARIABLE")
        val hasMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        @Suppress("UNUSED_VARIABLE")
        val hasModel = File(filesDir, WhisperTranscriber.MODEL_FILENAME).exists()
    }

    private fun downloadModel() {
        // TODO: download ggml-base.en.bin from HuggingFace into filesDir.
        // For now, the user can copy the model manually via adb:
        //   adb push ggml-base.en.bin /data/data/com.localtalk.android/files/
    }
}
