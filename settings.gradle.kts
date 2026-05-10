pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.1"
}

stonecutter {
    create(rootProject) {
        // Add or remove MC versions to support.
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        versions("1.21.11").buildscript("build.obfuscated.gradle.kts")
        versions("26.1.2").buildscript("build.unobfuscated.gradle.kts")


        // The version whose source tree lives in src/ (also used as VCS HEAD)
        vcsVersion = "26.1.2"
    }
}

rootProject.name = "Template"