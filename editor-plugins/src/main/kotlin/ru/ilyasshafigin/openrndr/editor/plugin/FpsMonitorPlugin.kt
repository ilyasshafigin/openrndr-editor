package ru.ilyasshafigin.openrndr.editor.plugin

import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorPlugin
import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.shapes.roundedRectangle
import ru.ilyasshafigin.openrndr.editor.resourceUrl
import kotlin.math.pow

class FpsMonitorPlugin : EditorPlugin {

    override val settings = Settings()

    /** */
    var refreshRate: Double = 0.5 //s

    private var elapsedTime: Double = 0.0 //s
    private var frames: Long = 0
    private var fps: Double = 0.0
    private lateinit var font: FontImageMap

    override fun setup(editor: Editor<*>) {
        font = loadFont(resourceUrl("font/Roboto-Regular.ttf"), 16.0)
    }

    override fun reset(editor: Editor<*>) {
        frames = 0
        fps = 0.0
    }

    override fun afterDraw(drawer: Drawer, editor: Editor<*>) {
        frames++
        elapsedTime += editor.deltaTime

        if (elapsedTime >= refreshRate) {
            fps = frames / elapsedTime
            elapsedTime = 0.0
            frames = 0
        }

        if (settings.isShowFps) {
            drawer.isolated {
                fill = mix(ColorRGBa.BLACK, ColorRGBa.RED, (1.0 - fps / 60.0).pow(2)).opacify(0.5)
                stroke = null

                val x = width - 80.0
                val y = 20.0
                val h = 30.0

                roundedRectangle(x, y, width - y - x, 30.0, 8.0)

                fontMap = font

                fill = ColorRGBa.WHITE
                text("${fps.toInt()} FPS", x + 8.0, y + h * 0.5 + 5.0)
            }
        }
    }

    @Description(title = "FPS")
    class Settings {

        @BooleanParameter(label = "Show FPS")
        var isShowFps: Boolean = true
    }
}
