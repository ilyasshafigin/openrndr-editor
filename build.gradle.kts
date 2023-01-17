plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
}

allprojects {
    group = "ru.ilyasshafigin.openrndr.editor"
    version = "0.3.0"
}
