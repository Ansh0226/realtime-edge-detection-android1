#include <jni.h>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <vector>
#include <cstdint>
#define LOG_TAG "NativeGLRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static GLuint textureId;
static int frameWidth = 0, frameHeight = 0;
static std::vector<uint8_t> frameBuffer;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_gl_NativeGLRenderer_initRenderer(JNIEnv *env, jobject thiz) {
    LOGI("✅ OpenGL Renderer initialized");

    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
}

JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_gl_NativeGLRenderer_updateFrame(
        JNIEnv *env, jobject thiz, jbyteArray data, jint width, jint height) {

    jsize len = env->GetArrayLength(data);
    jbyte *bytes = env->GetByteArrayElements(data, nullptr);

    frameWidth = width;
    frameHeight = height;
    frameBuffer.assign(bytes, bytes + len);

    env->ReleaseByteArrayElements(data, bytes, 0);
}

JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_gl_NativeGLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    glClear(GL_COLOR_BUFFER_BIT);

    if (frameBuffer.empty() || frameWidth == 0 || frameHeight == 0) return;

    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE,
                 frameWidth, frameHeight, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, frameBuffer.data());

    // Draw fullscreen quad (simple shader setup required, skipping for brevity)
}
}
