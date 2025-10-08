cmake_minimum_required(VERSION 3.10.2)

project("realtimeedgedetection")

# Enable modern C++
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# --- OpenCV include & lib directories ---
include_directories("../../../../sdk/native/jni/include")

# --- Add your native C++ sources ---
add_library(
        native-lib
        SHARED
        NativeProcessor.cpp
        NativeGLRenderer.cpp
)

# --- Import prebuilt OpenCV shared lib ---
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES
        IMPORTED_LOCATION "../../../../../sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so"
)

# --- Link everything ---
target_link_libraries(
        native-lib
        opencv_java4
        log
        android
        EGL
        GLESv2
        c++_shared
)
