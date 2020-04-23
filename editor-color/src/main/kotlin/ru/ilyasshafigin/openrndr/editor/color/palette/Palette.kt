package ru.ilyasshafigin.openrndr.editor.color.palette

import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

class Palette(colorCount: Int) {

    private val colors: Array<ColorRGBa> = createColors(colorCount)

    fun getRandomColor(alpha: Double = 1.0): ColorRGBa {
        return colors[Random.int0(colors.size)].opacify(alpha)
    }

    fun getAllColors(): Array<ColorRGBa> = colors

    companion object {

        private fun createColors(count: Int): Array<ColorRGBa> {
            val h = Random.double(0.0, 360.0)
            val s = Random.double(0.6, 1.0)
            val b = 1.0
            val d = 15.0
            return Array(count) { i ->
                var ch = (i - count * 0.5) * d + h
                if (ch < 0) ch += 360.0
                if (ch > 360) ch -= 360.0
                ColorHSVa(ch, s, b, 1.0).toRGBa()
            }
        }
    }
}
