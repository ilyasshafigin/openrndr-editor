package ru.ilyasshafigin.openrndr.editor.image

import ru.ilyasshafigin.openrndr.editor.color.toHex
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.colorBuffer

fun ColorBuffer.convertToRGBa(): ColorBuffer {
    val colorBuffer = colorBuffer(
        width = width,
        height = height,
        contentScale = contentScale,
        format = ColorFormat.RGBa
    )
    copyTo(colorBuffer)
    return colorBuffer
}

fun ColorBuffer.convertToRGB(): ColorBuffer {
    val colorBuffer = colorBuffer(
        width = width,
        height = height,
        contentScale = contentScale,
        format = ColorFormat.RGB
    )
    copyTo(colorBuffer)
    return colorBuffer
}

val ColorBuffer.pixels: IntArray
    get() {
        val arrays = shadow.mapInt { r, g, b, a -> ColorRGBa(r, g, b, a).toHex() }
        val result = IntArray(width * height)
        for (i in arrays.indices) {
            arrays[i].copyInto(result, i * width)
        }
        return result
    }
