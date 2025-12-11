plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.argumentor.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.argumentor.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 9
        versionName = "1.5.0"

        testInstrumentationRunner = "com.argumentor.app.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Optimize for performance in release builds
            isDebuggable = false
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            // Speed up debug builds
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Enable core library desugaring for java.time API on Android 7.0-7.1 (API 24-25)
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        // Optimization flags for Kotlin 2.0
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Baseline Profile Support (PERF-002)
    // Baseline Profiles improve app startup time by up to 30% and reduce jank during initial frames.
    // They enable Ahead-Of-Time (AOT) compilation of critical code paths.
    // The profile is defined in app/src/main/baseline-prof.txt and applied automatically
    // by ProfileInstaller at app startup.
    // See: https://developer.android.com/topic/performance/baselineprofiles

    // Gradle Managed Devices for CI (PERF-CI-001)
    // Enables fast instrumentation testing in CI using KVM-accelerated emulators
    // This replaces android-emulator-runner and enables Linux runners (60-70% faster than macOS)
    // See: https://developer.android.com/studio/test/gradle-managed-devices
    testOptions {
        managedDevices {
            devices {
                // API 28 device for testing
                create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2Api28") {
                    device = "Pixel 2"
                    apiLevel = 28
                    systemImageSource = "aosp"
                }
                // API 34 device for testing
                create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2Api34") {
                    device = "Pixel 2"
                    apiLevel = 34
                    systemImageSource = "aosp-atd" // Android Test Device images are optimized for CI
                }
            }
        }
        // Enable test sharding for parallel execution
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        // Return default values for empty test results
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Jetpack Compose (June 2024 BOM - has stable PullToRefresh API)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt Dependency Injection
    val hiltVersion = "2.52"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // JSON serialization
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Firebase - Crash Reporting & Analytics
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    // Baseline Profile Support (PERF-002)
    // ProfileInstaller automatically applies baseline profiles at app startup
    // This enables AOT compilation of critical code paths, improving startup time
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    // Core Library Desugaring - Required for java.time API on Android 7.0-7.1 (API 24-25)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing - Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.room:room-testing:$roomVersion")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.2.0")

    // Testing - Android Instrumented Tests
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("com.google.truth:truth:1.4.4")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("app.cash.turbine:turbine:1.2.0")
    kspAndroidTest("com.google.dagger:hilt-compiler:$hiltVersion")

    // Test Orchestrator for test sharding and isolation (PERF-CI-002)
    androidTestUtil("androidx.test:orchestrator:1.4.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
}
