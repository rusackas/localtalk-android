package com.localtalk.transcription

object WhisperJNI {
    init {
        System.loadLibrary("localtalk")
    }

    external fun loadModel(modelPath: String): Boolean
    external fun transcribe(pcmData: FloatArray): String
    external fun freeModel()
}
