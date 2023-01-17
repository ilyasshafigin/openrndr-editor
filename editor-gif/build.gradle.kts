plugins {
    id("editor-module")
}

dependencies {
    implementation(libs.lwjgl)
    implementation(libs.util.kotlinLogging)
    implementation(openrndr.core)
}
