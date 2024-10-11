import org.jetbrains.kotlin.gradle.dsl.JvmTarget


@Suppress("DSL_SCOPE_VIOLATION") // Remove when fixed https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "it.softsamurai"
    compileSdk = 35
    apply(
        plugin = "io.gitlab.arturbosch.detekt",
    )

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/gradle/detekt.yml"))
    }
    defaultConfig {
        applicationId = "it.softsamurai"
        minSdk = 30
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "it.softsamurai.core.testing.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }
    // The schemas directory contains a schema file for each version of the Room database.
    // This is required to enable Room auto migrations.
    // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    useLibrary("org.apache.http.legacy")
}

dependencies {

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.ui.text.google.fonts)
    ksp(libs.hilt.compiler)
    // Hilt and instrumented tests.
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    // Hilt and Robolectric tests.
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)

    // Arch Components
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //Arrow
    implementation(platform(libs.arrow.bom))
    implementation(libs.arrow.core)
    implementation(libs.arrow.optics)
    ksp(libs.arrow.optics.ksp.plugin)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.skrapeit) {
        exclude(group = "it.skrape", module = "skrapeit-async-fetcher")
    }
    // Tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Instrumented tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Local tests: jUnit, coroutines, Android runner
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented tests: jUnit rules and runners
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
