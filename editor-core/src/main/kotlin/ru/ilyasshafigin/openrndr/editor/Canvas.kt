package ru.ilyasshafigin.openrndr.editor

import mu.KotlinLogging
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.RenderTarget
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadImage
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

class Canvas {

    private val logger = KotlinLogging.logger { }

    private lateinit var _target: RenderTarget
    private var _width: Int = 0
    private var _height: Int = 0
    private lateinit var _center: Vector2

    /** Холст, на котором будет рисоваться обработанное изоражение */
    val target: RenderTarget get() = _target

    /** */
    val colorBuffer: ColorBuffer get() = target.colorBuffer(0)

    /** Width of drawing area, maybe more program window width. Independent of [EditorConfig.previewScale] */
    val width: Int get() = _width

    /** Height of drawing area, maybe more program window height. Independent of [EditorConfig.previewScale] */
    val height: Int get() = _height

    /** Center of drawing area */
    val center: Vector2 get() = _center

    fun setup(editor: Editor<*>) {
        _width = if (editor.config.format == EditorFormat.FULLSCREEN) editor.width else editor.config.format.width
        _height = if (editor.config.format == EditorFormat.FULLSCREEN) editor.height else editor.config.format.height
        _center = Vector2(_width * 0.5, _height * 0.5)
        _target = renderTarget(_width, _height) {
            colorBuffer()
            depthBuffer()
        }
    }

    fun draw(drawer: Drawer, block: Drawer.() -> Unit) {
        var drawException: RuntimeException? = null
        drawer.isolatedWithTarget(target) {
            ortho()
            try {
                block()
            } catch (e: RuntimeException) {
                logger.error(e) { "Canvas error" }
                drawException = e
            }
        }
        drawException?.let { drawer.drawRuntimeException(it) }
    }

    private fun Drawer.drawRuntimeException(exception: RuntimeException) = isolatedWithTarget(target) {
        val image = loadImage("data/images/Dizzy Face Emoji.png")
        image(
            image,
            Rectangle(0.0, 0.0, image.width.toDouble(), image.height.toDouble()),
            Rectangle(width * 0.5 - 100.0, height * 0.5 - 100.0, 200.0, 200.0)
        )
        image.destroy()
    }
}
