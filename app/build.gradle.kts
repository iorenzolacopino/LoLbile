plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lolbile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lolbile"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations.plus(listOf("en", "it"))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        generateLocaleConfig = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.ui.text)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.credentials:credentials:1.6.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation(platform("androidx.compose:compose-bom:2026.01.00"))
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.mayakapps.kache:kache:2.1.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}