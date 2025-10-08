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

    // Convert jbyteArray -> cv::Mat (YUV format)
    jbyte *data = env->GetByteArrayElements(frameData, nullptr);
    cv::Mat yuv(height + height / 2, width, CV_8UC1, data);
    cv::Mat bgr, gray, edges;

    // Convert YUV to BGR
    cv::cvtColor(yuv, bgr, cv::COLOR_YUV2BGR_NV21);

    // Convert to Grayscale
    cv::cvtColor(bgr, gray, cv::COLOR_BGR2GRAY);

    // Apply Canny Edge Detection
    cv::Canny(gray, edges, 100, 200);

    // Release buffer
    env->ReleaseByteArrayElements(frameData, data, 0);

    // Convert edges back to byte array
    jbyteArray result = env->NewByteArray(edges.total());
    env->SetByteArrayRegion(result, 0, edges.total(), reinterpret_cast<jbyte*>(edges.data));

    LOGI("✅ Frame processed with OpenCV: %dx%d", width, height);

    return result;
}