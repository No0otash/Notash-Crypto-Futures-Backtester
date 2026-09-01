import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.notash.cryptobacktester"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.notash.cryptobacktester"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "2.0.0"
    }
    buildFeatures { buildConfig = true }
    buildTypes {
        release { isMinifyEnabled = false; isShrinkResources = false }
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }
    }
    // Keys are injected at build time. Never commit real credentials.
    buildTypes.all {
        val moralisKey = providers.environmentVariable("MORALIS_API_KEY").orNull ?: ""
        val tokenomistKey = providers.environmentVariable("TOKENOMIST_API_KEY").orNull ?: ""
        buildConfigField("String", "MORALIS_API_KEY", "\"${moralisKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        buildConfigField("String", "TOKENOMIST_API_KEY", "\"${tokenomistKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(kotlin("test"))
}
