plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":editor-core"))
    implementation(project(":editor-plugins"))

    implementation(openrndr("core"))
    implementation(openrndr("ffmpeg"))
    implementation(openrndr("gl3"))
    implementation(openrndrNatives("gl3"))

    implementation(orx("orx-gui"))

    implementation(kotlin("stdlib"))
}
