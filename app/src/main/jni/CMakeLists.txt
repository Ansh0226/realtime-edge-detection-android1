# Minimum CMake version required
cmake_minimum_required(VERSION 3.4.1)

# Project name
project("realtimeedgedetection")
set(OpenCV_DIR "../../../../sdk/native/jni")
# --- Find OpenCV package ---
find_package(OpenCV REQUIRED)

# --- Add your native C++ source files ---
add_library(
        native-lib               # name of your shared library
        SHARED
        NativeProcessor.cpp      # file that will contain JNI + OpenCV logic
)

# --- Include OpenCV headers ---
include_directories(${OpenCV_INCLUDE_DIRS})

# --- Link OpenCV + Android + OpenGL ES libraries ---
target_link_libraries(
        native-lib
        ${OpenCV_LIBS}
        log
        android
        EGL
        GLESv2
)
