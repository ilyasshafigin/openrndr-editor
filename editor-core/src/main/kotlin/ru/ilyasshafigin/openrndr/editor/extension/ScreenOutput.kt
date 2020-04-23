package ru.ilyasshafigin.openrndr.editor.extension

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.shape.Rectangle

class ScreenOutput(
    private val buffer: ColorBuffer,
    area: Rectangle? = null,
    override var enabled: Boolean = true
) : Extension {

    private var dest: Rectangle? = area

    override fun afterDraw(drawer: Drawer, program: Program) {
        val rect = dest ?: Rectangle(0.0, 0.0, program.width * 1.0, program.height * 1.0)
            .also { dest = it }
        drawer.image(buffer, rect.x, rect.y, rect.width, rect.height)
    }
}
