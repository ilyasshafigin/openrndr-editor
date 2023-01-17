plugins {
    id("editor-module")
}

dependencies {
    implementation(project(":editor-core"))
    implementation(project(":editor-plugins"))

    implementation(libs.lwjgl)
    implementation(libs.util.kotlinLogging)
    implementation(openrndr.core)
    implementation(openrndr.ffmpeg)
    implementation(openrndr.gl3)
    implementation(openrndr.gl3Natives)

    runtimeOnly(libs.slf4j.simple)
}
