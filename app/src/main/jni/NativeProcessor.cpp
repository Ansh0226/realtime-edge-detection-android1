#include <jni.h>
#include <android/log.h>

#define LOG_TAG "NativeProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_MainActivity_testNative(JNIEnv *env, jobject thiz) {
    LOGI("✅ Native code successfully called!");
}
