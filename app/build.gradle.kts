plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "io.github.kdetard.koki"

    defaultConfig {
        applicationId = "io.github.kdetard.koki"
        minSdk = libs.versions.minsdk.get().toInt()
        compileSdk = libs.versions.compilesdk.get().toInt()
        targetSdk = libs.versions.targetsdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources.excludes.add("META-INF/rxjava.properties")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

    afterEvaluate {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.transition)
    implementation(libs.androidx.webkit)
    implementation(libs.appauth)
    implementation(libs.autodispose)
    implementation(libs.autodispose.android)
    implementation(libs.autodispose.lifecycle)
    implementation(libs.autodispose.androidx.lifecycle)
    implementation(libs.conductor)
    implementation(libs.conductor.androidx.transition)
    implementation(libs.conductor.viewpager2)
    implementation(libs.hilt.android)
    implementation(libs.insetter)
    implementation(libs.jsoup)
    implementation(libs.material)
    implementation(libs.mmkv)
    implementation(libs.moshi)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.adapter.rxjava3)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.rxdogtag)
    implementation(libs.rxdogtag.autodispose)
    implementation(libs.rxbinding)
    implementation(libs.rxbinding.core)
    implementation(libs.rxbinding.appcompat)
    implementation(libs.rxbinding.material)
    implementation(libs.rxbinding.recyclerview)
    implementation(libs.rxjava)
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    debugImplementation(libs.leakcanary)

    kapt(libs.hilt.android.compiler)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
    useBuildCache = true
}
