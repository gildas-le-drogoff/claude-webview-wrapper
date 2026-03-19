import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.gildas_le_drogoff.claudewebview"

    compileSdk = 36

    defaultConfig {
        applicationId = "fr.gildas_le_drogoff.claudewebview"
        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val fichierKeystore = rootProject.file("app/keystore.properties")
    val proprietesKeystore = Properties()
    if (fichierKeystore.exists()) {
        proprietesKeystore.load(fichierKeystore.inputStream())
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(proprietesKeystore["storeFile"] ?: "debug.keystore")
            storePassword = proprietesKeystore["storePassword"]?.toString() ?: "android"
            keyAlias = proprietesKeystore["keyAlias"]?.toString() ?: "androiddebugkey"
            keyPassword = proprietesKeystore["keyPassword"]?.toString() ?: "android"
        }
        create("release") {
            storeFile = file(proprietesKeystore["storeFile"] ?: "debug.keystore")
            storePassword = proprietesKeystore["storePassword"]?.toString() ?: "android"
            keyAlias = proprietesKeystore["keyAlias"]?.toString() ?: "androiddebugkey"
            keyPassword = proprietesKeystore["keyPassword"]?.toString() ?: "android"
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            isCrunchPngs = false
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}
kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.webkit)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
