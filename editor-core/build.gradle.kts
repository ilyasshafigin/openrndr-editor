plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":editor-color"))
    api(project(":editor-gcode"))
    api(project(":editor-gif"))
    api(project(":editor-image"))
    api(project(":editor-png"))
    api(project(":editor-shape"))
    api(project(":editor-svg"))

    implementation(openrndr("core"))
    implementation(openrndr("dialogs"))
    implementation(openrndr("ffmpeg"))

    implementation(orx("orx-gui"))
    implementation(orx("orx-glslify"))

    implementation(kotlinLogging())

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}