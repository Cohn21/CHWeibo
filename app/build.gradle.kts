plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    // KSP 替代 Kapt，支持 Java 21
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.chweibo.android"
    compileSdk = 34

    val localProperties = java.util.Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

    defaultConfig {
        applicationId = "com.chweibo.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "WEIBO_APP_KEY", "\"${localProperties.getProperty("WEIBO_APP_KEY", "")}\"")
        buildConfigField("String", "WEIBO_CLIENT_SECRET", "\"${localProperties.getProperty("WEIBO_CLIENT_SECRET", "")}\"")
        // 移动端回调地址，显示为 "Android手机" 而非 "Android上的Chrome"
        // 使用官方移动端回调地址格式
        buildConfigField("String", "WEIBO_REDIRECT_URI", "\"${localProperties.getProperty("WEIBO_REDIRECT_URI", "https://api.weibo.com/oauth2/default.html")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    // 禁用 JDK 镜像变换以解决 Java 21 兼容性问题
    androidResources {
        generateLocaleConfig = false
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material:1.6.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt (使用 KSP)
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-video:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    // Database (使用 KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Tink (Google Cryptography)
    implementation("com.google.crypto.tink:tink-android:1.19.0")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // Video Player
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // System UI Controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Swipe Refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

