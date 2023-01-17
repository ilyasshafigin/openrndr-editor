plugins {
    id("editor-module")
}

dependencies {
    implementation(project(":editor-core"))
    implementation(project(":editor-plugins"))

    implementation(openrndr.application)
    implementation(openrndr.core)
    implementation(openrndr.ffmpeg)
    implementation(openrndr.gl3)
    implementation(openrndr.gl3Natives)
    implementation(openrndr.orx.gui)
}
