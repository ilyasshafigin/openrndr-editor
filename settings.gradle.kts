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
            version("orx", if (orxUseSnapshot) "0.5.1-SNAPSHOT" else "0.4.2-rc.2")
            version("orml", if (ormlUseSnapshot) "0.5.1-SNAPSHOT" else "0.4.2-rc.2")

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("shadow", "com.github.johnrengelman.shadow").version("7.1.2")
            plugin("runtime", "org.beryx.runtime").version("1.12.7")

            plugin("gitarchive-tomarkdown", "org.openrndr.extra.gitarchiver.tomarkdown").versionRef("orx")

            library("kotlin-script-runtime", "org.jetbrains.kotlin", "kotlin-script-runtime").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")

            version("lwjgl", "3.3.1")
            library("lwjgl", "org.lwjgl", "lwjgl-stb").versionRef("lwjgl")

            version("slf4j", "1.7.36")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")

            version("kotlinx-coroutines", "1.6.4")
            library(
                "kotlinx-coroutines-core",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-core"
            ).versionRef("kotlinx-coroutines")

            library("util-kotlinLogging", "io.github.microutils:kotlin-logging-jvm:2.1.23")
            library("test-junit", "junit:junit:4.13.2")

            library("jsoup", "org.jsoup:jsoup:1.15.3")
            library("gson", "com.google.code.gson:gson:2.9.1")
            library("csv", "com.github.doyaaaaaken:kotlin-csv-jvm:1.7.0")
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
