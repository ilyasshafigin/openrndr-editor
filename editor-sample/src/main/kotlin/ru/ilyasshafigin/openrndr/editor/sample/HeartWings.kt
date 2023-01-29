package ru.ilyasshafigin.openrndr.editor.sample

import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import ru.ilyasshafigin.openrndr.editor.EditorFormat
import ru.ilyasshafigin.openrndr.editor.editor
import ru.ilyasshafigin.openrndr.editor.plugin.FpsMonitorPlugin
import ru.ilyasshafigin.openrndr.editor.plugin.GifRecorderPlugin
import ru.ilyasshafigin.openrndr.editor.plugin.VideoPreset
import ru.ilyasshafigin.openrndr.editor.plugin.VideoRecorderPlugin
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * https://www.openprocessing.org/sketch/377004/
 */
fun main() = editor(format = EditorFormat.HD_SQUARE) {
    val angles = (-180..180 step 1).map { it / 2.0 }
    val qs = (5..10).map { it / 5.0 }
    val circleList = mutableListOf<Vector2>()
    val radiusList = mutableListOf<Double>()

    install(FpsMonitorPlugin())
    install(GifRecorderPlugin())
    install(VideoRecorderPlugin(VideoPreset.REAL_TIME))

    draw {
        canvas.draw(drawer) {
            clear(ColorRGBa.fromHex(0x262526))
            fill = ColorRGBa.fromHex(0xf60a20).opacify(0.6)
            stroke = null

            translate(center)

            circleList.clear()
            radiusList.clear()

            for (angle in angles) {
                for (q in qs) {
                    val a = q * 180
                    val t = angle + frameCount * 1.5 + a
                    val x = 16 * sin(toRadians(t)).pow(3)
                    val y = -13 * cos(toRadians(t)) + 5 * cos(toRadians(2 * t)) + 2 * cos(toRadians(3 * t)) + cos(
                        toRadians(4 * t)
                    )
                    val s = cos(toRadians(angle)) * q * 7

                    circleList += Vector2(x * q * 8, y * q * 8)
                    radiusList += s
                }
            }

            circles(circleList, radiusList)
        }
    }
}
