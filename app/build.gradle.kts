plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
    alias(libs.plugins.com.google.dagger.hilt.android)
}

android {
    namespace = "io.github.ktard.koki"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.ktard.koki"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    afterEvaluate {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.appauth)
    implementation(libs.rxjava)
    implementation(libs.rxbinding)
    implementation(libs.rxbinding.core)
    implementation(libs.rxbinding.appcompat)
    implementation(libs.rxbinding.material)
    implementation(libs.rxbinding.recyclerview)
    implementation(libs.androidx.webkit)
    implementation(libs.mmkv)
    implementation(libs.cookie.store)
    implementation(libs.cookie.store.okhttp)
    implementation(libs.retrofit)
    implementation(libs.cookie.store)
    implementation(libs.hilt.android)
    implementation(libs.androidx.browser)
    implementation(libs.adapter.rxjava3)
    implementation(libs.logging.interceptor)
    implementation(libs.jsoup)
    kapt(libs.hilt.android.compiler)
    implementation(libs.moshi)
    implementation(libs.moshi)
    implementation(libs.autodispose)
    implementation(libs.autodispose.android)
    implementation(libs.autodispose.lifecycle)
    implementation(libs.autodispose.androidx.lifecycle)
    implementation(libs.rxdogtag.autodispose)
    implementation(libs.rxdogtag)
    implementation(libs.timber)
    implementation(libs.converter.moshi)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
