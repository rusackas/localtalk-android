package com.localtalk.ime

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.localtalk.transcription.WhisperTranscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class KeyboardView(
    context: Context,
    private val transcriber: WhisperTranscriber
) : FrameLayout(context) {

    var onTextReady: ((String) -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isRecording = false

    private val micButton: ImageButton
    private val statusLabel: TextView
    private val spinner: ProgressBar

    init {
        setBackgroundColor(Color.parseColor("#1E1E1E"))

        statusLabel = TextView(context).apply {
            text = if (transcriber.isReady()) "Hold to speak" else "Model not loaded — open LocalTalk to set up"
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
        }

        micButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setBackgroundColor(Color.parseColor("#3A3A3A"))
            isEnabled = transcriber.isReady()
        }

        spinner = ProgressBar(context).apply {
            visibility = View.GONE
        }

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 32, 48, 32)
            addView(statusLabel, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 })
            addView(micButton, android.widget.LinearLayout.LayoutParams(200, 200))
            addView(spinner, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 })
        }

        addView(layout, LayoutParams(LayoutParams.MATCH_PARENT, 280))

        setupMicButton()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMicButton() {
        micButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isRecording) startRecording()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isRecording) stopRecording()
                    true
                }
                else -> false
            }
        }
    }

    private fun startRecording() {
        isRecording = true
        statusLabel.text = "Listening…"
        micButton.setBackgroundColor(Color.parseColor("#CC0000"))
        spinner.visibility = View.GONE
    }

    private fun stopRecording() {
        isRecording = false
        micButton.setBackgroundColor(Color.parseColor("#3A3A3A"))
        statusLabel.text = "Transcribing…"
        spinner.visibility = View.VISIBLE

        scope.launch {
            val text = transcriber.recordAndTranscribe()
            spinner.visibility = View.GONE
            statusLabel.text = "Hold to speak"
            if (text.isNotEmpty()) {
                onTextReady?.invoke("$text ")
            }
        }
    }
}
