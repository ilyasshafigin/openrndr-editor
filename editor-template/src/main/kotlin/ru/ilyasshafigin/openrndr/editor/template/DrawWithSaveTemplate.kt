package ru.ilyasshafigin.openrndr.editor.template

import org.openrndr.color.ColorRGBa
import ru.ilyasshafigin.openrndr.editor.EditorFormat
import ru.ilyasshafigin.openrndr.editor.editor
import ru.ilyasshafigin.openrndr.editor.plugin.ExportImagePlugin

fun main() = editor(format = EditorFormat.HD_SQUARE) {
    install(ExportImagePlugin())

    reset {
        canvas.draw(drawer) {
            clear(ColorRGBa.WHITE)
            fill = ColorRGBa.BLACK
            circle(width * 0.5, height * 0.5, 300.0)
        }
    }

    draw {

    }
}
