buildscript {
    dependencies {
        classpath(libs.kotlin.gradle)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

tasks.register<Delete>("clean") {
    description = "Deletes the root build directory."
    delete(rootProject.layout.buildDirectory)
}
