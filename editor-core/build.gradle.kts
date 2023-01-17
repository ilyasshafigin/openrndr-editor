plugins {
    id("editor-module")
}

dependencies {
    api(project(":editor-color"))
    api(project(":editor-image"))
    api(project(":editor-shape"))

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.util.kotlinLogging)
    implementation(openrndr.application)
    implementation(openrndr.core)
    implementation(openrndr.orx.gui)
}
