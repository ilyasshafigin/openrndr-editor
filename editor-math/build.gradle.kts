plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":editor-image"))

    implementation(openrndr("core"))

    implementation(orx("orx-noise"))

    implementation(kotlin("stdlib"))
}
