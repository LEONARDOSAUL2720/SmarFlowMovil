plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.smartflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartflow"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    // Material Design (versión única)
    implementation("com.google.android.material:material:1.10.0")
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    // WebView mejorado
    implementation("androidx.webkit:webkit:1.7.0")
    implementation(libs.volley)
    // JSON Processing
    implementation("com.google.code.gson:gson:2.10.1")
    // WebView enhanced
    implementation("androidx.webkit:webkit:1.8.0")
    // Fragment support (para DatePicker)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}