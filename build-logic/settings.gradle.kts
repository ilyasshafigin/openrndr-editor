enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.openrndr.org")
    }
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.0")
            library("kotlin-gradlePlugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
        }
    }
}

include(
    ":convention",
)
