package ru.ilyasshafigin.openrndr.editor.extension

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

class ScreenOutput(
    private val buffer: ColorBuffer,
    target: Rectangle? = null,
    override var enabled: Boolean = true
) : Extension {

    private var _target: Rectangle? = target

    override fun afterDraw(drawer: Drawer, program: Program) {
        val target = _target?.takeIf { it.width.toInt() == program.width && it.height.toInt() == program.height }
            ?: Rectangle(Vector2.ZERO, program.width.toDouble(), program.height.toDouble())
                .also { _target = it }
        drawer.image(buffer, buffer.bounds, target)
    }
}
