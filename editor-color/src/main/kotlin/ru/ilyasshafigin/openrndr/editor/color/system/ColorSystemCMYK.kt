package ru.ilyasshafigin.openrndr.editor.color.system

import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

class ColorSystemCMYK : ColorSystem {

    override fun getColor(color: ColorRGBa): ColorSystem.Color = Color(color)

    override fun getComponentColorByType(type: ComponentType): ColorRGBa = when (type) {
        CYAN_TYPE -> ColorRGBa.fromHex(0x00ffff)
        MAGENTA_TYPE -> ColorRGBa.fromHex(0xff00ff)
        YELLOW_TYPE -> ColorRGBa.YELLOW
        BLACK_TYPE -> ColorRGBa.BLACK
        else -> ColorRGBa.TRANSPARENT
    }

    override fun getRandomComponentType(): ComponentType {
        return Random.int0(4)
    }

    class Color(color: ColorRGBa) : ColorSystem.Color {

        private val cyan: Double
        private val magenta: Double
        private val yellow: Double
        private val black: Double
        override val brightness: Double

        init {
            var r1 = color.r
            val g1 = color.g
            val b1 = color.b
            if (r1 < g1) r1 = g1
            black = 1 - maxOf(r1, g1, b1)
            cyan = (1 - r1 - black) / (1 - black)
            magenta = (1 - g1 - black) / (1 - black)
            yellow = (1 - b1 - black) / (1 - black)
            brightness = color.toHSVa().v
        }

        override fun getComponentByType(type: ComponentType): Double = when (type) {
            CYAN_TYPE -> cyan
            MAGENTA_TYPE -> magenta
            YELLOW_TYPE -> yellow
            BLACK_TYPE -> black
            else -> 0.0
        }
    }

    companion object {

        const val CYAN_TYPE: ComponentType = 0
        const val MAGENTA_TYPE: ComponentType = 1
        const val YELLOW_TYPE: ComponentType = 2
        const val BLACK_TYPE: ComponentType = 3
    }
}
