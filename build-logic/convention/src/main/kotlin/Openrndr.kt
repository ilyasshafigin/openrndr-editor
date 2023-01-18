@file:Suppress("INACCESSIBLE_TYPE")

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

private var _os: String? = null

val Project.openrndr: Openrndr
    get() = extensions.findByType<OpenrndrDepsExtension>()?.openrndr
        ?: OpenrndrDepsExtension(this).also { extensions.add<OpenrndrDepsExtension>("openrndr", it) }.openrndr

val Project.openrndrOs: String
    get() = _os ?: getCurrentOs().also { _os = it }

private val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private val Project.openrndrVersion: String
    get() = libs.findVersion("openrndr").get().toString()

private val Project.orxVersion: String
    get() = libs.findVersion("openrndr-orx").get().toString()

private val Project.ormlVersion: String
    get() = libs.findVersion("openrndr-orml").get().toString()

private fun Project.getCurrentOs(): String = if (hasProperty("targetPlatform")) {
    val supportedPlatforms = setOf("windows", "macos", "linux-x64", "linux-arm64")
    val platform: String = property("targetPlatform") as String
    if (platform !in supportedPlatforms) {
        throw IllegalArgumentException("target platform not supported: $platform")
    } else {
        platform
    }
} else when (OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "windows"
    OperatingSystem.MAC_OS -> when (DefaultNativePlatform("current").architecture.name) {
        "aarch64", "arm-v8" -> "macos-arm64"
        else -> "macos"
    }

    OperatingSystem.LINUX -> when (val h = DefaultNativePlatform("current").architecture.name) {
        "x86-64" -> "linux-x64"
        "aarch64" -> "linux-arm64"
        else -> throw IllegalArgumentException("architecture not supported: $h")
    }

    else -> throw IllegalArgumentException("os not supported")
}

private class OpenrndrDepsExtension(
    project: Project
) {

    val openrndr = Openrndr(
        openrndrVersion = project.openrndrVersion,
        orxVersion = project.orxVersion,
        ormlVersion = project.ormlVersion,
        os = project.openrndrOs,
    )
}

/*
 * https://github.com/openrndr/openrndr
 */
class Openrndr(
    openrndrVersion: String,
    orxVersion: String,
    ormlVersion: String,
    os: String,
    group: String = "org.openrndr"
) {

    val animatable = "$group:openrndr-animatable:$openrndrVersion"
    val application = "$group:openrndr-application:$openrndrVersion"
    val binpack = "$group:openrndr-binpack:$openrndrVersion"
    val color = "$group:openrndr-color:$openrndrVersion"
    val core = "$group:openrndr-core:$openrndrVersion"
    val dds = "$group:openrndr-dds:$openrndrVersion"
    val dialogs = "$group:openrndr-dialogs:$openrndrVersion"
    val draw = "$group:openrndr-draw:$openrndrVersion"
    val event = "$group:openrndr-event:$openrndrVersion"
    val extensions = "$group:openrndr-extensions:$openrndrVersion"
    val filter = "$group:openrndr-filter:$openrndrVersion"
    val math = "$group:openrndr-math:$openrndrVersion"
    val nullgl = "$group:openrndr-nullgl:$openrndrVersion"
    val shape = "$group:openrndr-shape:$openrndrVersion"
    val svg = "$group:openrndr-svg:$openrndrVersion"
    val utils = "$group:openrndr-svg:$openrndrVersion"

    val gl3 = "$group:openrndr-gl3:$openrndrVersion"
    val gl3Natives = "$group:openrndr-gl3-natives-$os:$openrndrVersion"
    val openal = "$group:openrndr-openal:$openrndrVersion"
    val openalNatives = "$group:openrndr-openal-natives-$os:$openrndrVersion"
    val ffmpeg = "$group:openrndr-ffmpeg:$openrndrVersion"
    val ffmpegNatives = "$group:openrndr-ffmpeg-natives-$os:$openrndrVersion"

    val orx = Orx(orxVersion, os)
    val orml = Orml(ormlVersion)
}

class Orx(
    version: String,
    os: String,
    group: String = "org.openrndr.extra",
) {

    val boofcv = "$group:orx-boofcv:$version"
    val camera = "$group:orx-camera:$version"
    val chataigne = "$group:orx-chataigne:$version"
    val color = "$group:orx-color:$version"
    val compositor = "$group:orx-compositor:$version"
    val computeGraph = "$group:orx-compute-graph:$version"
    val computeGraphNodes = "$group:orx-compute-graph-nodes:$version"
    val dnk3 = "$group:orx-dnk3:$version"
    val easing = "$group:orx-easing:$version"
    val fileWatcher = "$group:orx-file-watcher:$version"
    val filterExtension = "$group:orx-filter-extension:$version"
    val fx = "$group:orx-fx:$version"
    val glslify = "$group:orx-glslify:$version"
    val gradientDescent = "$group:orx-gradient-descent:$version"
    val gitArchiver = "$group:orx-git-archiver:$version"
    val gui = "$group:orx-gui:$version"
    val imageFit = "$group:orx-image-fit:$version"
    val integralImage = "$group:orx-integral-image:$version"
    val intervalTree = "$group:orx-interval-tree:$version"
    val jumpflood = "$group:orx-jumpflood:$version"
    val kdtree = "$group:orx-kdtree:$version"
    val keyframer = "$group:orx-keyframer:$version"
    val kinectV1 = "$group:orx-kinect-v1:$version"
    val kinectV1Natives = "$group:orx-kinect-v1-natives-$os:$version"
    val kotlinParser = "$group:orx-kotlin-parser:$version"
    val meshGenerators = "$group:orx-mesh-generators:$version"
    val midi = "$group:orx-midi:$version"
    val minim = "$group:orx-minim:$version"
    val noClear = "$group:orx-no-clear:$version"
    val noise = "$group:orx-noise:$version"
    val objLoader = "$group:orx-obj-loader:$version"
    val olive = "$group:orx-olive:$version"
    val osc = "$group:orx-osc:$version"
    val palette = "$group:orx-palette:$version"
    val panel = "$group:orx-panel:$version"
    val parameters = "$group:orx-parameters:$version"
    val poissonFill = "$group:orx-poisson-fill:$version"
    val rabbitControl = "$group:orx-rabbit-control:$version"
    val realsense2 = "$group:orx-realsense2:$version"
    val runway = "$group:orx-runway:$version"
    val shadeStyles = "$group:orx-shade-styles:$version"
    val shaderPhrases = "$group:orx-shader-phrases:$version"
    val shapes = "$group:orx-shapes:$version"
    val syphon = "$group:orx-syphon:$version"
    val temporalBlur = "$group:orx-temporal-blur:$version"
    val tensorflow = "$group:orx-tensorflow:$version"
    val tensorflowNatives = "$group:orx-tensorflow-natives-$os:$version"
    val tensorflowNativesGpu = "$group:orx-tensorflow-gpu-natives-$os:$version"
    val timeOperators = "$group:orx-time-operators:$version"
    val timer = "$group:orx-timer:$version"
    val triangulation = "$group:orx-triangulation:$version"
    val videoProfiles = "$group:orx-video-profiles:$version"
}

class Orml(
    version: String,
    group: String = "org.openrndr.orml"
) {

    val blazepose = "$group:orml-blazepose:$version"
    val dbface = "$group:orml-dbface:$version"
    val facemesh = "$group:orml-facemesh:$version"
    val imageClassifier = "$group:orml-image-classifier:$version"
    val psenet = "$group:orml-psenet:$version"
    val ssd = "$group:orml-ssd:$version"
    val styleTransfer = "$group:orml-style-transfer:$version"
    val superResolution = "$group:orml-super-resolution:$version"
    val u2net = "$group:orml-u2net:$version"
}
