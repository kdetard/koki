import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.Date
import java.util.Properties

val versionMajor = 0
val versionMinor = 1
val versionPatch = 0

fun getBuildNumber(): Int {
    val df = SimpleDateFormat("yyyyMMdd")
    val date = LocalDateTime.now()
    val seconds =
            (Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).seconds / 86400) * 99.0
    val twoDigitSuffix = seconds.toInt()

    return Integer.parseInt(df.format(Date()) + String.format("%02d", twoDigitSuffix))
}

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "io.github.kdetard.koki"

    compileSdk = libs.versions.compilesdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.kdetard.koki"
        minSdk = libs.versions.minsdk.get().toInt()
        targetSdk = libs.versions.targetsdk.get().toInt()
        versionCode = getBuildNumber()
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MAPTILER_API_KEY", "\"${gradleLocalProperties(rootDir).getProperty("MAPTILER_API_KEY")}\"")
    }

    signingConfigs {
        create("release") {
            val properties = Properties()
            properties.load(FileInputStream(project.rootProject.file("local.properties")))

            storeFile = file(properties.getProperty("signing.storeFilePath"))
            storePassword = properties.getProperty("signing.storePassword")
            keyAlias = properties.getProperty("signing.keyAlias")
            keyPassword = properties.getProperty("signing.keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }

        debug {
            isDebuggable = true
        }
    }

    packaging {
        resources.excludes.add("META-INF/rxjava.properties")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        // Disable some unused things
        aidl = false
        renderScript = false
        shaders = false
        buildConfig = true
    }

    afterEvaluate {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.add("-Xlint:unchecked")
            options.compilerArgs.add("-Xlint:deprecation")
        }
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.rxjava3)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.transition)
    implementation(libs.appauth)
    implementation(libs.autodispose)
    implementation(libs.autodispose.android)
    implementation(libs.autodispose.lifecycle)
    implementation(libs.autodispose.androidx.lifecycle)
    implementation(libs.conductor)
    implementation(libs.conductor.androidx.transition)
    implementation(libs.conductor.viewpager2)
    implementation(libs.fastadapter)
    implementation(libs.fastadapter.extensions.binding)
    implementation(libs.fastadapter.extensions.expandable)
    implementation(libs.fastadapter.extensions.ui)
    implementation(libs.fastadapter.extensions.utils)
    implementation(libs.hilt.android)
    implementation(libs.insetter)
    implementation(libs.jsoup)

    implementation(libs.maplibre.android.sdk) {
        exclude(libs.timber.get().group, libs.timber.get().name)
    }

    implementation(libs.maplibre.android.plugin.annotation.v9) {
        exclude(libs.timber.get().group, libs.timber.get().name)
    }

    implementation(libs.material)
    implementation(libs.mmkv)
    implementation(libs.moshi)
    implementation(libs.moshi.polymorphic.adapter)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.protobuf.javalite)
    implementation(libs.retrofit)
    implementation(libs.retrofit.adapter.rxjava3)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.rxdogtag)
    implementation(libs.rxdogtag.autodispose)
    implementation(libs.rxbinding)
    implementation(libs.rxbinding.appcompat)
    implementation(libs.rxbinding.core)
    implementation(libs.rxbinding.drawerlayout)
    implementation(libs.rxbinding.material)
    implementation(libs.rxbinding.recyclerview)
    implementation(libs.rxbinding.slidingpanelayout)
    implementation(libs.rxbinding.swiperefreshlayout)
    implementation(libs.rxjava)
    implementation(libs.rxpm)
    implementation(libs.timber)
    implementation(libs.vico.core)
    implementation(libs.vico.views)
    implementation(libs.leakcanary.watcher)

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

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach {
            it.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
