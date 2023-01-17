plugins {
    id("editor-module")
}

dependencies {
    implementation(project(":editor-math"))

    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))

    implementation(openrndr.core)
}
