// androidApp/build.gradle.kts

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    // *** Remove `package="..."` from your AndroidManifest.xml ***
    namespace   = "com.example.physiciannotes.android"

    compileSdk  = 34

    defaultConfig {
        applicationId = "com.example.physiciannotes.android"
        minSdk        = 24
        targetSdk     = 34

        versionCode   = 1
        versionName   = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // If you call JVM APIs from Kotlin code
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Match your Compose BOM / library versions
        kotlinCompilerExtensionVersion = "1.5.0"
    }

    // Work around duplicate META-INF warnings
    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":shared"))

    // AndroidX
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.foundation:foundation:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("com.google.android.material:material:1.10.0")
}

