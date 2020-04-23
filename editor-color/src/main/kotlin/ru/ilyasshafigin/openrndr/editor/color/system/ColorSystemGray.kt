package ru.ilyasshafigin.openrndr.editor.color.system

import org.openrndr.color.ColorRGBa

class ColorSystemGray : ColorSystem {

    override fun getColor(color: ColorRGBa): ColorSystem.Color = Color(color)

    override fun getComponentColorByType(type: ComponentType): ColorRGBa = ColorRGBa.BLACK

    override fun getRandomComponentType(): ComponentType = 0

    class Color(color: ColorRGBa) : ColorSystem.Color {

        override val brightness: Double = color.toHSVa().v

        override fun getComponentByType(type: Int): Double = brightness
    }
}
