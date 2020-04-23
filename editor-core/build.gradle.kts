plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":editor-color"))
    implementation(project(":editor-gcode"))
    implementation(project(":editor-gif"))
    implementation(project(":editor-image"))
    implementation(project(":editor-png"))
    implementation(project(":editor-shape"))
    implementation(project(":editor-svg"))

    implementation(openrndr("core"))
    implementation(openrndr("dialogs"))
    implementation(openrndr("ffmpeg"))

    implementation(orx("orx-gui"))
    implementation(orx("orx-glslify"))

    implementation(kotlinLogging())

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}
