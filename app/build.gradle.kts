plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.alexandresamson.freelancereceipt"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alexandresamson.freelancereceipt"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig URLs — change in one place when domain is final
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://freelancereceipt.app/privacy\"")
        buildConfigField("String", "TERMS_URL",          "\"https://freelancereceipt.app/terms\"")
        buildConfigField("String", "SUPPORT_EMAIL",      "\"support@freelancereceipt.app\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Fix für die 16-KB-Warnung
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {

    // ── Room ────────────────────────────────────────────────────────────────
    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // ── Compose BOM + UI ────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended")
    // Version kommt vom BOM — kein hardcoded "1.6.8" nötig

    // ── Splash Screen ────────────────────────────────────────────────────────
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ── Google Play Billing ──────────────────────────────────────────────────
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // ── DataStore (Preferences) ──────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Core & Lifecycle ─────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // ── Navigation ───────────────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // ── Biometrie ────────────────────────────────────────────────────────────
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    // Stabile Alternative falls alpha unerwünscht:
    // implementation("androidx.biometric:biometric:1.1.0")

    // ── Koin ─────────────────────────────────────────────────────────────────
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // ── Firebase ─────────────────────────────────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // ── Google Sign-In (moderner Credentials-Stack) ──────────────────────────
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // play-services-auth wird vom Credentials-Stack intern gezogen
    // — kein direkter Eintrag mehr nötig

    // ── CameraX ──────────────────────────────────────────────────────────────
    val camerax_version = "1.4.2"
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

    // ── ML Kit ───────────────────────────────────────────────────────────────
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // ── Berechtigungen ───────────────────────────────────────────────────────
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // ── Testing ──────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}