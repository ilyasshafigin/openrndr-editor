plugins {
    id("editor-module")
}

dependencies {
    api(project(":editor-color"))
    api(project(":editor-core"))
    api(project(":editor-gcode"))
    api(project(":editor-gif"))
    api(project(":editor-image"))
    api(project(":editor-png"))
    api(project(":editor-shape"))
    api(project(":editor-svg"))

    implementation(libs.util.kotlinLogging)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(openrndr.core)
    implementation(openrndr.dialogs)
    implementation(openrndr.ffmpeg)
    implementation(openrndr.orx.glslify)
    implementation(openrndr.orx.noise)
    implementation(openrndr.orx.parameters)
    implementation(openrndr.orx.shapes)
}
