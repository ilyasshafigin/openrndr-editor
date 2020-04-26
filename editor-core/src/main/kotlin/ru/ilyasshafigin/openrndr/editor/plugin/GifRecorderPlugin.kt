package ru.ilyasshafigin.openrndr.editor.plugin

import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorPlugin
import ru.ilyasshafigin.openrndr.editor.gif.GifWriter
import mu.KotlinLogging
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.isolated
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.events.Event
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GifRecorderPlugin : EditorPlugin {

    override val settings = @Description(title = "Gif recorder") object {

        @ActionParameter("Begin recording")
        fun beginRecording() = performBeginRecording()

        @ActionParameter("Begin recording in 10 sec")
        fun beginRecording10sec() = performBeginRecording(10)

        @ActionParameter("Begin recording in 30 sec")
        fun beginRecording30sec() = performBeginRecording(30)

        @ActionParameter("End recording")
        fun endRecording() = performEndRecording()
    }

    val profile: GifWriterProfile = GifWriterProfile()
    var exportDirectory = "" // init on setup
    var exportFileDatePattern = "YYYYMMddHHmmss"

    val beginRecording = Event<BeginGifRecording>("editor-gif-recording-begin").postpone(true)
    val endRecording = Event<EndGifRecording>("editor-gif-recording-end").postpone(true)

    private lateinit var colorBuffer: ColorBuffer
    private lateinit var gifWriter: GifWriter
    private lateinit var gifTarget: RenderTarget

    private val logger = KotlinLogging.logger {}
    private var isRecording = false
    private var timestamp = 0L
    private var recordingDuration = -1 //sec
    private var frames = 0

    override fun setup(editor: Editor<*>) {
        colorBuffer = editor.canvas.colorBuffer
        exportDirectory = "export/${editor.name.toLowerCase()}/gif"

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
            val imageName = sourceFileName.ifEmpty { editor.name.toLowerCase() }
            val filename = "$exportDirectory/$imageName-${fileDateFormat.format(Date(timestamp))}.gif"

            File(filename).parentFile.let { folder ->
                if (!folder.exists()) {
                    folder.mkdirs()
                }
            }

            logger.info { "Begin gif recording: $filename" }

            gifWriter = GifWriter.create()
                .output(filename)
                .size(colorBuffer.width, colorBuffer.height)
                .frameRate(profile.frameRate)
                .repeat(profile.repeat)
                .quality(profile.quality)
                .transparentColor(profile.transparentColor)
                .dispose(profile.dispose)
                .start()
            gifTarget = gifWriter.createRenderTarget()
            isRecording = true
        }

        endRecording.listen { (isTimeout) ->
            logger.info {
                val duration = (frames / profile.frameRate).toInt()
                if (isTimeout) {
                    "End gif recording by timeout, duration: $duration sec"
                } else {
                    "End gif recording, duration: $duration sec"
                }
            }
            isRecording = false
            gifWriter.stop()
            timestamp = 0L
            recordingDuration = -1
            frames = 0
        }
    }

    override fun beforeDraw(drawer: Drawer, editor: Editor<*>) {
        beginRecording.deliver()
        endRecording.deliver()
    }

    override fun afterDraw(drawer: Drawer, editor: Editor<*>) {
        if (isRecording) {
            drawer.isolatedWithTarget(gifTarget) {
                ortho(gifTarget)
                image(colorBuffer)
            }
            gifWriter.frame(gifTarget.colorBuffer(0))

            drawer.isolated {
                fill = null
                stroke = ColorRGBa.BLUE
                strokeWeight = 3.0
                rectangle(0.0, 0.0, width * 1.0, height * 1.0)
            }

            frames++
            if (recordingDuration > 0.0) {
                val duration =  frames / profile.frameRate
                if (duration >= recordingDuration) {
                    performEndRecording(isTimeout = true)
                }
            }
        }
    }

    private fun performBeginRecording(duration: Int = -1) {
        beginRecording.trigger(BeginGifRecording(duration))
    }

    private fun performEndRecording(isTimeout: Boolean = false) {
        endRecording.trigger(EndGifRecording(isTimeout))
    }
}

data class BeginGifRecording(val duration: Int)
data class EndGifRecording(val isTimeout: Boolean)

/**
 * @param transparentColor color to be treated as transparent on display
 * @param repeat int number of iterations, 0 = infinite
 * @param frameRate frames per second
 * @param quality quality of color quantization, lower values produce better colors, minimum = 1
 * @param dispose disposal mode for the last added frame, values [GifWriter.DISPOSE_NOTHING], [GifWriter.DISPOSE_KEEP],
 *   [GifWriter.DISPOSE_RESTORE_BACKGROUND], [GifWriter.DISPOSE_REMOVE]
 */
data class GifWriterProfile(
    val transparentColor: ColorRGBa? = null,
    val repeat: Int = 0,
    val frameRate: Double = 60.0,
    val quality: Int = 10,
    val dispose: Int = -1
)

val Editor<*>.gifRecorder: GifRecorderPlugin
    get() = getPlugin()
