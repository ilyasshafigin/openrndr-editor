package ru.ilyasshafigin.openrndr.editor.gif

import mu.KotlinLogging
import org.lwjgl.BufferUtils
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.renderTarget
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

class GifWriter private constructor() {

    private var encoder: GifEncoder? = null
    private var fileStream: OutputStream? = null
    private var isFirstFrame: Boolean = true
    private lateinit var frameBuffer: ByteBuffer
    private lateinit var flippedBuffer: ByteBuffer

    private var fileName: String = "rndr.gif"
    private var width: Int = 320
    private var height: Int = 240
    private var transparentColor: ColorRGBa? = null
    private var repeat: Int = -1
    private var delay: Int = 0
    private var dispose: Int = -1 // -1 = use default
    private var quality: Int = 10

    /**
     * Sets of containing output file name
     */
    fun output(fileName: String) = apply {
        this.fileName = fileName
    }

    /**
     * Sets the size of the GIF-file. if this method is not invoked, the size of
     * the first added frame will be the image size.
     */
    fun size(width: Int, height: Int) = apply {
        require(width % 2 == 0 && height % 2 == 0) {
            "width ($width) and height ($height) should be divisible by 2"
        }
        this.width = width
        this.height = height
    }

    /**
     * Sets the transparent color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the quantization
     * process, the color in the final palette for each frame closest to the given
     * color becomes the transparent color for that frame. May be set to null to
     * indicate no transparent color.
     *
     * @param color Color to be treated as transparent on display.
     */
    fun transparentColor(color: ColorRGBa?) = apply {
        this.transparentColor = color
    }

    /**
     * Sets the number of times the set of GIF frames should be played. Default is -1;
     * 0 means play indefinitely. Must be invoked before the first image is added.
     *
     * @param repeat int number of iterations.
     */
    fun repeat(repeat: Int) = apply {
        this.repeat = repeat
    }

    /**
     * Sets the delay time between each frame, or changes it for subsequent frames
     * (applies to last frame added).
     *
     * @param delay int delay time in milliseconds
     */
    fun delay(delay: Int) = apply {
        this.delay = (delay / 10.0).roundToInt()
    }

    /**
     * Sets frame rate in frames per second. Equivalent to `delay(1000/fps)`.
     *
     * @param fps frame rate (frames per second)
     */
    fun frameRate(fps: Double) = apply {
        if (fps > 0.0) {
            this.delay = (100 / fps).roundToInt()
        }
    }

    /**
     * Set the disposal mode for the last added frame
     *
     * From GIF specs:
     * * 00 CODE MEANING - Nothing special
     * * 01 KEEP - retain the current image
     * * 02 RESTORE BACKGROUND - restore the background color
     * * 03 REMOVE - remove the current image, and restore whatever image was beneath it
     */
    fun dispose(dispose: Int) = apply {
        if (dispose >= 0) {
            this.dispose = dispose
        }
    }

    /**
     * Sets quality of color quantization (conversion of images to the maximum 256 colors allowed by
     * the GIF specification). Lower values (minimum = 1) produce better colors,
     * but slow processing significantly. 10 is the default, and produces good
     * color mapping at reasonable speeds. Values greater than 20 do not yield
     * significant improvements in speed.
     *
     * @param quality int greater than 0.
     */
    fun quality(quality: Int) = apply {
        this.quality = if (quality < 1) 1 else quality
    }

    /**
     *
     */
    fun createRenderTarget(): RenderTarget {
        return renderTarget(width, height) {
            colorBuffer(ColorFormat.RGB)
            depthBuffer()
        }
    }

    /**
     * Start writing to the GIF file
     */
    fun start(): GifWriter {
        logger.debug { "starting gif writer with $width x $height output writing to $fileName" }

        check(width > 0) { "invalid width or width not set $width" }
        check(height > 0) { "invalid height or height not set $height" }

        frameBuffer = BufferUtils.createByteBuffer(width * height * ColorFormat.RGB.componentCount)
        flippedBuffer = BufferUtils.createByteBuffer(width * height * ColorFormat.RGB.componentCount)
        isFirstFrame = true

        try {
            fileStream = BufferedOutputStream(FileOutputStream(fileName))
            logger.debug { "created file stream for $fileName" }
            encoder = GifEncoder(width, height, transparentColor, repeat, delay, dispose)
            logger.debug { "created gif encoder" }
            encoder!!.start(fileStream!!)
            logger.debug { "started gif encoder" }

            return this
        } catch (exception: IOException) {
            throw RuntimeException("Failed to start Gif writer", exception)
        }
    }

    fun frame(frame: ColorBuffer) {
        check(frame.width == width && frame.height == height) { "Frame size mismatch" }
        check(frame.format == ColorFormat.RGB) { "Frame color format not equals RGB" }
        checkNotNull(fileStream) { "File stream not opened" }

        frameBuffer.rewind()
        frameBuffer.order(ByteOrder.nativeOrder())
        frame.read(frameBuffer)
        frameBuffer.rewind()

        if (!frame.flipV) {
            flippedBuffer.rewind()
            val stride = width * frame.format.componentCount
            val row = ByteArray(stride)
            for (y in 0 until height) {
                frameBuffer.position((height - y - 1) * stride)
                frameBuffer.get(row)
                flippedBuffer.put(row)
            }

            flippedBuffer.rewind()
            frameBuffer.rewind()
            frameBuffer.put(flippedBuffer)
            frameBuffer.rewind()
        }

        try {
            val pixels = ByteArray(frameBuffer.remaining())
            frameBuffer.get(pixels)
            encoder?.write(fileStream!!, pixels, isFirstFrame)
            if (isFirstFrame) {
                isFirstFrame = false
            }
        } catch (exception: IOException) {
            throw IllegalStateException("Failed to write frame", exception)
        }
    }

    /**
     * Finishes off the GIF-file and saves it to the given filename
     * in the sketch directory. if the file already exists, it will
     * be overridden!
     */
    fun stop(): GifWriter {
        try {
            encoder?.stop(fileStream ?: throw IllegalStateException("File stream not opened"))
            fileStream?.close()
            encoder = null
            fileStream = null
            return this
        } catch (exception: IOException) {
            throw IllegalStateException("failed to close the gif stream", exception)
        } finally {
            encoder = null
            fileStream = null
        }
    }

    companion object {

        const val DISPOSE_NOTHING = 0
        const val DISPOSE_KEEP = 1
        const val DISPOSE_RESTORE_BACKGROUND = 2
        const val DISPOSE_REMOVE = 3

        fun create(): GifWriter = GifWriter()
    }
}
