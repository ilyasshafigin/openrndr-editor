import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.internal.os.OperatingSystem
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

val openrndrVersion = "0.3.42-rc.3"
val orxVersion = "0.3.51-rc.3"
val lwjglVersion = "3.2.3"
val loggingVersion = "1.7.9"

val supportedPlatforms = setOf("windows", "macos", "linux-x64", "linux-arm64")

val openrndrOs: String by lazy {
//    if (project.hasProperty("targetPlatform")) {
//        val platform: String = project.property("targetPlatform") as String
//        if (platform !in supportedPlatforms) {
//            throw IllegalArgumentException("target platform not supported: $platform")
//        } else {
//            platform
//        }
//    } else
    when (OperatingSystem.current()) {
        OperatingSystem.WINDOWS -> "windows"
        OperatingSystem.MAC_OS -> "macos"
        OperatingSystem.LINUX -> when (val h = DefaultNativePlatform("current").architecture.name) {
            "x86-64" -> "linux-x64"
            "aarch64" -> "linux-arm64"
            else -> throw IllegalArgumentException("architecture not supported: $h")
        }
        else -> throw IllegalArgumentException("os not supported")
    }
}

fun DependencyHandler.orx(module: String): Any {
    return "org.openrndr.extra:$module:$orxVersion"
}

fun DependencyHandler.openrndr(module: String): Any {
    return "org.openrndr:openrndr-$module:$openrndrVersion"
}

fun DependencyHandler.openrndrNatives(module: String): Any {
    return "org.openrndr:openrndr-$module-natives-$openrndrOs:$openrndrVersion"
}

fun DependencyHandler.orxNatives(module: String): Any {
    return "org.openrndr.extra:$module-natives-$openrndrOs:$orxVersion"
}

fun DependencyHandler.kotlinLogging(): Any {
    return "io.github.microutils:kotlin-logging:$loggingVersion"
}

fun DependencyHandler.lwjgl(): Any {
    return "org.lwjgl:lwjgl-stb:$lwjglVersion"
}
