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

    implementation(kotlin("stdlib"))
    implementation(kotlinLogging())

    runtimeOnly("org.slf4j","slf4j-simple","1.7.30")
}
