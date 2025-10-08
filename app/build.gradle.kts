plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.realtimeedgedetection"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.realtimeedgedetection"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ NDK setup for native C++ integration
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
        packaging {
            jniLibs {
                useLegacyPackaging = true
            }
        }


        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-frtti", "-fexceptions")
//                this line to specify the androidx . media3 . common . C++ standard library
                        arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    // ✅ Enable ViewBinding for easier UI access
    buildFeatures {
        viewBinding = true
    }

    // ✅ Link with your native C++ build system
    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.core:core-ktx:1.12.0")

    // ✅ Add OpenCV for Android
//    implementation("org.opencv:opencv-android:4.10.0")
//    implementation(libs.opencv)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
