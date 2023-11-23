buildscript {
    val aboutLibrariesVersion by extra("10.9.1")
    val composeVersion by extra("1.6.0-beta01")

    repositories {
        google()
        mavenCentral()
        //NEEDED FOR ABOUTLIBRARIES
        maven { url = uri("https://plugins.gradle.org/m2/")}
    }

    dependencies {
        //ABOUT LIBRARIES
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:$aboutLibrariesVersion")
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}