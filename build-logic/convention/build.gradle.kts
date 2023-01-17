plugins {
    `kotlin-dsl`
}

group = "ru.ilyasshafigin.openrndr.buildlogic"

dependencies {
    implementation(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("openrndr-deps") {
            id = "openrndr-deps"
            implementationClass = "OpenrndrDepsPlugin"
        }
        register("editor-module") {
            id = "editor-module"
            implementationClass = "EditorModulePlugin"
        }
    }
}
