package ru.ilyasshafigin.openrndr.editor.sample

import org.openrndr.application
import ru.ilyasshafigin.openrndr.editor.color.palette.ColorSpace
import ru.ilyasshafigin.openrndr.editor.color.palette.PaletteClustering
import ru.ilyasshafigin.openrndr.editor.color.palette.PaletteDistance
import ru.ilyasshafigin.openrndr.editor.color.palette.generatePalette
import ru.ilyasshafigin.openrndr.editor.color.toHexString
import ru.ilyasshafigin.openrndr.editor.editor

fun main() = application {
    editor {
        reset {
            val count = 5
            val space = ColorSpace.default
            val clustering = PaletteClustering.K_MEANS
            val distance = PaletteDistance.DEUTERANOPE
            val quality = 50
            val ultraPrecision = false
            val colors = generatePalette(
                colorCount = count,
                colorSpace = space,
                clustering = clustering,
                distance = distance,
                quality = quality,
                ultraPrecision = ultraPrecision
            )

            logger.info { colors.joinToString { it.toHexString() } }

            canvas.draw(drawer) {
                val step = width.toDouble() / colors.size
                colors.forEachIndexed { i, color ->
                    fill = color
                    stroke = null

                    rectangle(i * step, 0.0, step, height.toDouble())
                }
            }
        }
    }
}
