#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "whisper.cpp/include/whisper.h"

#define LOG_TAG "LocalTalkJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

static whisper_context* g_ctx = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_localtalk_transcription_WhisperJNI_loadModel(JNIEnv* env, jobject, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    whisper_context_params params = whisper_context_default_params();
    g_ctx = whisper_init_from_file_with_params(path, params);
    env->ReleaseStringUTFChars(modelPath, path);
    if (!g_ctx) {
        LOGE("Failed to load model from %s", path);
        return JNI_FALSE;
    }
    LOGI("Model loaded");
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_localtalk_transcription_WhisperJNI_transcribe(JNIEnv* env, jobject,
                                                        jfloatArray pcmData) {
    if (!g_ctx) return env->NewStringUTF("");

    jsize len = env->GetArrayLength(pcmData);
    jfloat* pcm = env->GetFloatArrayElements(pcmData, nullptr);

    whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.language = "en";
    wparams.translate = false;
    wparams.print_progress = false;
    wparams.print_timestamps = false;
    wparams.single_segment = false;
    wparams.no_context = true;

    std::string result;
    if (whisper_full(g_ctx, wparams, pcm, (int)len) == 0) {
        int n = whisper_full_n_segments(g_ctx);
        for (int i = 0; i < n; i++) {
            result += whisper_full_get_segment_text(g_ctx, i);
        }
    } else {
        LOGE("whisper_full failed");
    }

    env->ReleaseFloatArrayElements(pcmData, pcm, JNI_ABORT);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_localtalk_transcription_WhisperJNI_freeModel(JNIEnv*, jobject) {
    if (g_ctx) {
        whisper_free(g_ctx);
        g_ctx = nullptr;
    }
}

} // extern "C"
