plugins {
    id("editor-module")
}

dependencies {
    implementation(project(":editor-color"))

    implementation(libs.lwjgl)
    implementation(openrndr.core)
}
