plugins {
    alias(libs.plugins.android.application)
}

val productionBaseUrl = "https://api.fluxcloud.dev"
val productionWsUrl = "wss://api.fluxcloud.dev/ws"
val localBaseUrl = (project.findProperty("SOCIAL_HAZARD_LOCAL_BASE_URL") as String?)
    ?.takeIf { it.isNotBlank() }
    ?: "http://10.0.2.2:8080"
val localWsUrl = (project.findProperty("SOCIAL_HAZARD_LOCAL_WS_URL") as String?)
    ?.takeIf { it.isNotBlank() }
    ?: "ws://10.0.2.2:8080/ws"
val useLocalBackend = (project.findProperty("SOCIAL_HAZARD_USE_LOCAL_BACKEND") as String?)
    ?.toBoolean()
    ?: false
val configuredBaseUrl = if (useLocalBackend) localBaseUrl else productionBaseUrl
val configuredWsUrl = if (useLocalBackend) localWsUrl else productionWsUrl
val configuredBackendMode = if (useLocalBackend) "local" else "production"

android {
    namespace = "com.socialhazard.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.socialhazard.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "BASE_URL", "\"$configuredBaseUrl\"")
        buildConfigField("String", "WS_URL", "\"$configuredWsUrl\"")
        buildConfigField("boolean", "USE_LOCAL_BACKEND", useLocalBackend.toString())
        buildConfigField("String", "BACKEND_MODE", "\"$configuredBackendMode\"")

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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.gson)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.okhttp)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
