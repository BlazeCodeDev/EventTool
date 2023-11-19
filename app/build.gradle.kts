plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    namespace = "com.blazecode.eventtool"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blazecode.eventtool"
        minSdk = 33
        targetSdk = 34
        versionCode = 18
        versionName = "1.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

val aboutLibrariesVersion: String by rootProject.extra
val composeVersion: String by rootProject.extra

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ROOM DB
    val room_version = "2.6.0"
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // ACCOMPANIST
    val accompanist_version = "0.33.2-alpha"
    implementation("com.google.accompanist:accompanist-flowlayout:$accompanist_version")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist_version")
    implementation("com.google.accompanist:accompanist-permissions:$accompanist_version")

    // CALENDAR
    implementation("com.kizitonwose.calendar:compose:2.4.0")

    // NAVIGATION
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // GSON
    implementation("com.google.code.gson:gson:2.10.1")

    // LOTTIE
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    //ABOUT LIBRARIES
    implementation("com.mikepenz:aboutlibraries-compose:$aboutLibrariesVersion")

    //DATA STORE
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // VOLLEY
    implementation("com.android.volley:volley:1.2.1")

    // COMPOSE DATE TIME PICKER
    implementation("com.marosseleng.android:compose-material3-datetime-pickers:0.7.2")

    // CRASH DETECTION
    val acraVersion = "5.11.3"
    implementation("ch.acra:acra-mail:$acraVersion")
    implementation("ch.acra:acra-dialog:$acraVersion")
    implementation("com.google.guava:guava:32.1.3-jre")
}