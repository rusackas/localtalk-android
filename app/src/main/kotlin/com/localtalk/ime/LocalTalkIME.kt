package com.localtalk.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import com.localtalk.transcription.WhisperTranscriber

class LocalTalkIME : InputMethodService() {

    private lateinit var transcriber: WhisperTranscriber
    private lateinit var keyboardView: KeyboardView

    override fun onCreate() {
        super.onCreate()
        transcriber = WhisperTranscriber(applicationContext)
        transcriber.loadModel()
    }

    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(this, transcriber)
        keyboardView.onTextReady = { text ->
            currentInputConnection?.commitText(text, 1)
        }
        return keyboardView
    }

    override fun onDestroy() {
        transcriber.release()
        super.onDestroy()
    }
}
