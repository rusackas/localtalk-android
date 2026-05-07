package com.localtalk.transcription

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WhisperTranscriber(context: Context) {

    companion object {
        const val SAMPLE_RATE = 16000
        private const val MODEL_FILENAME = "ggml-base.en.bin"
        // Download from: https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin
    }

    private val modelFile = File(context.filesDir, MODEL_FILENAME)
    private var isModelLoaded = false

    fun isReady() = isModelLoaded && modelFile.exists()

    fun loadModel(): Boolean {
        if (!modelFile.exists()) return false
        isModelLoaded = WhisperJNI.loadModel(modelFile.absolutePath)
        return isModelLoaded
    }

    suspend fun recordAndTranscribe(durationMs: Long = 5000): String = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val totalSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
        val buffer = ShortArray(totalSamples)
        var offset = 0

        recorder.startRecording()
        while (offset < totalSamples) {
            val read = recorder.read(buffer, offset, minOf(bufferSize, totalSamples - offset))
            if (read <= 0) break
            offset += read
        }
        recorder.stop()
        recorder.release()

        val pcm = FloatArray(offset) { buffer[it] / 32768f }
        WhisperJNI.transcribe(pcm).trim()
    }

    fun release() {
        if (isModelLoaded) {
            WhisperJNI.freeModel()
            isModelLoaded = false
        }
    }
}
