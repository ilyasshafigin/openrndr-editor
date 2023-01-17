plugins {
    id("editor-module")
}

dependencies {
    implementation(project(":editor-image"))

    implementation(openrndr.core)
    implementation(openrndr.orx.noise)
}
