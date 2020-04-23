plugins {
    kotlin("jvm")
}

dependencies {
    implementation(openrndr("core"))

    implementation(lwjgl())
    implementation(kotlinLogging())

    implementation(kotlin("stdlib"))
}
