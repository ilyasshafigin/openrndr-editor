pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

val openrndrUseSnapshot = false
val orxUseSnapshot = false
val ormlUseSnapshot = true

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.openrndr.org")
    }

    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.0")
            version("openrndr", if (openrndrUseSnapshot) "0.5.1-SNAPSHOT" else "0.4.2-rc.2")
            version("openrndr-orx", if (orxUseSnapshot) "0.5.1-SNAPSHOT" else "0.4.2-rc.2")
            version("openrndr-orml", if (ormlUseSnapshot) "0.5.1-SNAPSHOT" else "0.4.2-rc.2")
            version("lwjgl", "3.3.1")
            version("util-slf4j", "1.7.36")

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("shadow", "com.github.johnrengelman.shadow").version("7.1.2")
            plugin("runtime", "org.beryx.runtime").version("1.12.7")

            library("lwjgl", "org.lwjgl", "lwjgl-stb").versionRef("lwjgl")
            library("kotlin-script-runtime", "org.jetbrains.kotlin", "kotlin-script-runtime").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            version("kotlinx-coroutines", "1.6.4")
            library(
                "kotlinx-coroutines-core",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-core"
            ).versionRef("kotlinx-coroutines")

            library("util-kotlinLogging", "io.github.microutils:kotlin-logging-jvm:2.1.23")
            library("util-slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("util-slf4j")
            library("test-junit", "junit:junit:4.13.2")
        }
    }
}

rootProject.name = "openrndr-editor"

include(
    "editor-color",
    "editor-core",
    "editor-gcode",
    "editor-gif",
    "editor-image",
    "editor-math",
    "editor-plugins",
    "editor-png",
    "editor-sample",
    "editor-shape",
    "editor-svg",
    "editor-template"
)
