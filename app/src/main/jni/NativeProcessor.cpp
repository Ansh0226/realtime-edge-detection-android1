#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "NativeProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_MainActivity_testNative(JNIEnv *env, jobject thiz) {
    LOGI("✅ Native code successfully called!");
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_realtimeedgedetection_MainActivity_processFrame(
        JNIEnv *env, jobject thiz,
        jbyteArray frameData, jint width, jint height) {

    jbyte *data = env->GetByteArrayElements(frameData, nullptr);

    // ✅ Input is tight grayscale Y plane, size = width*height
    cv::Mat gray(height, width, CV_8UC1, data);
    cv::Mat edges;
    cv::Canny(gray, edges, 100, 200);

    if (!edges.isContinuous()) {
        edges = edges.clone();
    }

    env->ReleaseByteArrayElements(frameData, data, 0);

    const int n = width * height;
    jbyteArray result = env->NewByteArray(n);
    env->SetByteArrayRegion(result, 0, n, reinterpret_cast<jbyte*>(edges.data));

//    LOGI("✅ Frame processed with OpenCV: %dx%d", width, height);

    return result;
}
