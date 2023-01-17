package ru.ilyasshafigin.openrndr.editor.plugin

import mu.KotlinLogging
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.isolated
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.events.Event
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.ffmpeg.VideoWriter
import org.openrndr.ffmpeg.VideoWriterProfile
import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorPlugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoRecorderPlugin(preset: VideoPreset = VideoPreset.HIGH_QUALITY) : EditorPlugin {

    override val settings = @Description(title = "Video recorder") object {

        @ActionParameter("Begin recording")
        fun beginRecording() = performBeginRecording()

        @ActionParameter("Begin recording in 10 sec")
        fun beginRecording10sec() = performBeginRecording(10)

        @ActionParameter("Begin recording in 30 sec")
        fun beginRecording30sec() = performBeginRecording(30)

        @ActionParameter("End recording")
        fun endRecording() = performEndRecording()
    }

    var exportDirectory = "" // init on setup
    var exportFileDatePattern = "YYYYMMddHHmmss"

    val profile: VideoWriterProfile = when(preset) {
        VideoPreset.REAL_TIME -> newRealTimeVideoWriterProfile()
        VideoPreset.HIGH_QUALITY -> newHighQualityVideoWriterProfile()
        VideoPreset.INSTAGRAM -> newInstagramVideoWriterProfile()
    }

    val beginRecording = Event<BeginVideoRecording>("editor-video-recording-begin").apply { postpone = true }
    val endRecording = Event<EndVideoRecording>("editor-video-recording-end").apply { postpone = true }

    private lateinit var colorBuffer: ColorBuffer
    private lateinit var videoWriter: VideoWriter
    private lateinit var videoTarget: RenderTarget

    private val logger = KotlinLogging.logger {}
    private val inputFrameRate = 60
    private var isRecording = false
    private var timestamp = 0L
    private var recordingDuration = -1 //sec
    private var frames = 0

    override fun setup(editor: Editor<*>) {
        exportDirectory = "export/${editor.name.lowercase()}/mp4"
        colorBuffer = editor.canvas.colorBuffer
        videoTarget = renderTarget(colorBuffer.width , colorBuffer.height) {
            colorBuffer()
        }

        beginRecording.listen { (duration) ->
            if (isRecording) {
                logger.info { "Recording is already running" }
                return@listen
            }

            timestamp = System.currentTimeMillis()
            recordingDuration = duration

            val fileDateFormat = SimpleDateFormat(exportFileDatePattern, Locale.getDefault())
            val sourceFileName = if (editor.isPluginInstalled<SourcePlugin>()) {
                editor.source.fileName
            } else {
                editor.name
            }
            val imageName = sourceFileName.ifEmpty { editor.name.lowercase() }
            val filename = "$exportDirectory/$imageName-${fileDateFormat.format(Date(timestamp))}.mp4"

            File(filename).parentFile.let { folder ->
                if (!folder.exists()) {
                    folder.mkdirs()
                }
            }

            logger.info { "Begin recording: $filename" }

            videoWriter = VideoWriter()
                .profile(profile)
                .output(filename)
                .size(colorBuffer.width, colorBuffer.height)
                .frameRate(inputFrameRate)
                .start()
            isRecording = true
        }

        endRecording.listen { (isTimeout) ->
            if (isRecording) {
                logger.info {
                    val duration = (frames * 1.0 / inputFrameRate).toInt()
                    if (isTimeout) {
                        "End recording by timeout, duration: $duration sec"
                    } else {
                        "End recording, duration: $duration sec"
                    }
                }
                isRecording = false
                videoWriter.stop()
                timestamp = 0L
                recordingDuration = -1
                frames = 0
            }
        }
    }

    override fun beforeDraw(drawer: Drawer, editor: Editor<*>) {
        beginRecording.deliver()
        endRecording.deliver()
    }

    override fun afterDraw(drawer: Drawer, editor: Editor<*>) {
        if (isRecording) {
            drawer.isolatedWithTarget(videoTarget) {
                ortho(videoTarget)
                image(colorBuffer)
            }
            videoWriter.frame(videoTarget.colorBuffer(0))

            drawer.isolated {
                fill = null
                stroke = ColorRGBa.RED
                strokeWeight = 3.0
                rectangle(0.0, 0.0, width * 1.0, height * 1.0)
            }

            frames++
            if (recordingDuration > 0.0) {
                val duration =  frames * 1.0 / inputFrameRate
                if (duration >= recordingDuration) {
                    performEndRecording(isTimeout = true)
                }
            }
        }
    }

    private fun performBeginRecording(duration: Int = -1) {
        beginRecording.trigger(BeginVideoRecording(duration))
    }

    private fun performEndRecording(isTimeout: Boolean = false) {
        endRecording.trigger(EndVideoRecording(isTimeout))
    }
}

data class BeginVideoRecording(val duration: Int)
data class EndVideoRecording(val isTimeout: Boolean)

enum class VideoPreset {
    REAL_TIME,
    HIGH_QUALITY,
    INSTAGRAM
}

val Editor<*>.videoRecorder: VideoRecorderPlugin
    get() = getPlugin()

fun newRealTimeVideoWriterProfile(
    constantRateFactor: Int = 10, // 0 would mean lossless, 10 seems like a reasonable value
    // to prevent blocky artifacts on gradients, ffmpeg default is 23
    frameRate: Double = 60.0
) : VideoWriterProfile = object : VideoWriterProfile() {
    override val fileExtension: String = "mp4"
    override fun arguments(): Array<String> = arrayOf(
        "-vcodec", "libx264",
        "-pix_fmt", "yuv420p",
        "-an",
        "-vf", "vflip, colorspace=bt709:iall=bt601-6-625:fast=1",
        "-crf", "$constantRateFactor",
        "-r", "$frameRate",
        "-preset", "ultrafast",
        "-sws_flags", "spline+accurate_rnd+full_chroma_int",
        "-color_range", "1",
        "-color_primaries", "1",
        "-color_trc", "1",
        "-colorspace", "1"
    )
}

fun newHighQualityVideoWriterProfile(
    constantRateFactor: Int = 10, // 0 would mean lossless, 10 seems like a reasonable value
    // to prevent blocky artifacts on gradients, ffmpeg default is 23
    frameRate: Double = 60.0
) : VideoWriterProfile = object : VideoWriterProfile() {
    override val fileExtension: String = "mp4"
    override fun arguments(): Array<String> = arrayOf(
        "-vcodec", "libx264",
        "-pix_fmt", "yuv420p",
        "-an",
        "-vf", "vflip, colorspace=bt709:iall=bt601-6-625:fast=1",
        "-crf", "$constantRateFactor",
        "-r", "$frameRate",
        "-preset", "slow",
        "-tune", "film",
        "-movflags", "+faststart",
        "-sws_flags", "spline+accurate_rnd+full_chroma_int",
        "-color_range", "1",
        "-color_primaries", "1",
        "-color_trc", "1",
        "-colorspace", "1"
    )
}

fun newInstagramVideoWriterProfile(
    constantRateFactor: Int = 20, // seems that maybe it should go really up for instagram?
    frameRate: Double = 30000.0 / 1001.0
) : VideoWriterProfile = object : VideoWriterProfile() {
    override val fileExtension: String = "mp4"
    override fun arguments(): Array<String> = arrayOf(
        "-vcodec", "libx264",
        "-pix_fmt", "yuv420p",
        "-an",
        "-vf", "vflip, colorspace=bt709:iall=bt601-6-625:fast=1",
        "-crf", "$constantRateFactor",
        "-r", "$frameRate",
        "-preset", "veryslow",
        "-tune", "film",
        "-level", "4.0",
        "-color_primaries", "1",
        "-color_trc", "1",
        "-colorspace", "1",
        "-color_range", "1",
        "-movflags", "+faststart"
    )
}
