import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.3.72" apply false
}

allprojects {
    group = "ru.ilyasshafigin.openrndr.editor"
    version = "0.2.0"

    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/openrndr/openrndr")
    }
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }
}
