package ru.ilyasshafigin.openrndr.editor.image

import org.lwjgl.stb.STBImageResize
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.colorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Image(
    val colorBuffer: ColorBuffer
) {

    val width: Int
        get() = colorBuffer.effectiveWidth

    val height: Int
        get() = colorBuffer.effectiveHeight

    private val buffer = ByteBuffer.allocateDirect(width * height * colorBuffer.format.componentCount)
        .order(ByteOrder.nativeOrder())

    fun loadPixels() {
        colorBuffer.shadow.download()
    }

    fun updatePixels() {
        colorBuffer.shadow.upload()
    }

    fun resized(newWidth: Int, newHeight: Int): Image {
        val newColorBuffer = colorBuffer(
            width = newWidth,
            height = newHeight,
            contentScale = colorBuffer.contentScale,
            format = colorBuffer.format
        )
        val newBuffer = ByteBuffer.allocateDirect(newWidth * newHeight * colorBuffer.format.componentCount)
            .order(ByteOrder.nativeOrder())

        buffer.clear()
        colorBuffer.read(buffer)

        STBImageResize.stbir_resize_uint8(
            buffer,
            width,
            height,
            0,
            newBuffer,
            newWidth,
            newHeight,
            0,
            colorBuffer.format.componentCount
        )

        newColorBuffer.write(newBuffer)

        return Image(newColorBuffer)
    }

    fun get(x: Int, y: Int, default: ColorRGBa = ColorRGBa.TRANSPARENT): ColorRGBa {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return default
        }

        return colorBuffer.shadow[x, y]
    }

    fun set(x: Int, y: Int, color: ColorRGBa) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return
        }
        colorBuffer.shadow[x, y] = color
    }

    fun destroy() {
        colorBuffer.destroy()
    }
}
