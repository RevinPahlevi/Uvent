plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.uventapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uventapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true
    }
}

dependencies {

    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- DEPENDENSI UNTUK API (SAYA TAMBAHKAN) ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    // ---------------------------------------------


    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Coil untuk load gambar event (poster)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // WorkManager for background tasks (notification scheduling)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // SwipeRefresh untuk pull-to-refresh (notification screen)
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")
    
    // Firebase BOM (manages all Firebase library versions)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Firebase Analytics (optional but recommended)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}
