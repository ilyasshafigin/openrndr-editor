plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":editor-color"))
    api(project(":editor-image"))
    api(project(":editor-shape"))

    implementation(openrndr("core"))

    implementation(orx("orx-gui"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation(kotlinLogging())

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}
