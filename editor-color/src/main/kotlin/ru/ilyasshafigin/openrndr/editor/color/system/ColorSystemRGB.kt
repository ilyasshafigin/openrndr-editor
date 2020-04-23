package ru.ilyasshafigin.openrndr.editor.color.system

import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random

class ColorSystemRGB : ColorSystem {

    override fun getColor(color: ColorRGBa): ColorSystem.Color = Color(color)

    override fun getComponentColorByType(type: ComponentType): ColorRGBa = when (type) {
        RED_TYPE -> ColorRGBa.RED
        GREEN_TYPE -> ColorRGBa.GREEN
        BLUE_TYPE -> ColorRGBa.BLUE
        else -> ColorRGBa.TRANSPARENT
    }

    override fun getRandomComponentType(): ComponentType {
        return Random.int0(3)
    }

    class Color(private val color: ColorRGBa) : ColorSystem.Color {

        override val brightness: Double = color.toHSVa().v

        override fun getComponentByType(type: Int): Double = when (type) {
            RED_TYPE -> color.r
            GREEN_TYPE -> color.g
            BLUE_TYPE -> color.b
            else -> 0.0
        }
    }


    companion object {

        const val RED_TYPE: ComponentType = 0
        const val GREEN_TYPE: ComponentType = 1
        const val BLUE_TYPE: ComponentType = 2
    }
}
