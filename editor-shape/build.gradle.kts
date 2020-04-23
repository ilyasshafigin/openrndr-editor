plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":editor-math"))

    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))

    implementation(openrndr("core"))

    implementation(kotlin("stdlib"))
}
