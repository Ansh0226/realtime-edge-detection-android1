#include <jni.h>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <vector>

#define LOG_TAG "NativeGLRenderer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static GLuint program;
static GLuint textureId;
static GLint positionHandle, texCoordHandle, textureHandle;

static int frameWidth = 0, frameHeight = 0;
static std::vector<uint8_t> frameBuffer;

const char *VERTEX_SHADER =
        "attribute vec4 aPosition;\n"
        "attribute vec2 aTexCoord;\n"
        "varying vec2 vTexCoord;\n"
        "void main() {\n"
        "  gl_Position = aPosition;\n"
        "  vTexCoord = aTexCoord;\n"
        "}";

const char *FRAGMENT_SHADER =
        "precision mediump float;\n"
        "varying vec2 vTexCoord;\n"
        "uniform sampler2D uTexture;\n"
        "void main() {\n"
        "  float gray = texture2D(uTexture, vTexCoord).r;\n"
        "  gl_FragColor = vec4(gray, gray, gray, 1.0);\n"
        "}";

GLuint compileShader(GLenum type, const char *src) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &src, nullptr);
    glCompileShader(shader);
    return shader;
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_gl_NativeGLRenderer_initRenderer(JNIEnv *env, jobject thiz) {
    LOGI("✅ initRenderer called");

    GLuint vShader = compileShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    GLuint fShader = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

    program = glCreateProgram();
    glAttachShader(program, vShader);
    glAttachShader(program, fShader);
    glLinkProgram(program);
    glUseProgram(program);

    positionHandle = glGetAttribLocation(program, "aPosition");
    texCoordHandle = glGetAttribLocation(program, "aTexCoord");
    textureHandle = glGetUniformLocation(program, "uTexture");

    // Generate texture
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    // ✅ Bind texture unit 0
    glUseProgram(program);
    glUniform1i(textureHandle, 0);
}

JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_gl_NativeGLRenderer_updateFrame(
        JNIEnv *env, jobject thiz, jbyteArray data, jint width, jint height) {

    jsize len = env->GetArrayLength(data);
    jbyte *bytes = env->GetByteArrayElements(data, nullptr);

    frameWidth = width;
    frameHeight = height;

    // ✅ ensure buffer has exact size
    frameBuffer.assign(bytes, bytes + (width * height));

    env->ReleaseByteArrayElements(data, bytes, 0);
}

JNIEXPORT void JNICALL
Java_com_example_realtimeedgedetection_gl_NativeGLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    glClear(GL_COLOR_BUFFER_BIT);

    if (frameBuffer.empty() || frameWidth == 0 || frameHeight == 0) return;

    glBindTexture(GL_TEXTURE_2D, textureId);

    // ✅ ensure correct row alignment
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE,
                 frameWidth, frameHeight, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, frameBuffer.data());

    // ✅ Properly interleaved position + texcoord array
    // ✅ Rotate texture 90° to the right (clockwise)
    // ✅ Rotate 90° clockwise and correct vertical flip
    // ✅ Flip horizontally (no rotation)
    const GLfloat vertices[] = {
            -1.f, -1.f,  1.f, 0.f,
            1.f, -1.f,  1.f, 1.f,
            -1.f,  1.f,  0.f, 0.f,
            1.f,  1.f,  0.f, 1.f,
    };




    const int stride = 4 * sizeof(GLfloat);

    glEnableVertexAttribArray(positionHandle);
    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, stride, vertices);

    glEnableVertexAttribArray(texCoordHandle);
    glVertexAttribPointer(texCoordHandle, 2, GL_FLOAT, GL_FALSE, stride, vertices + 2);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}

}
