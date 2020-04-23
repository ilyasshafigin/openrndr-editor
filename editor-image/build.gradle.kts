plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":editor-color"))

    implementation(openrndr("core"))

    implementation(lwjgl())

    implementation(kotlin("stdlib"))
}
