package ru.ilyasshafigin.openrndr.editor.template

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import ru.ilyasshafigin.openrndr.editor.EditorFormat
import ru.ilyasshafigin.openrndr.editor.editor
import ru.ilyasshafigin.openrndr.editor.plugin.ExportImagePlugin
import ru.ilyasshafigin.openrndr.editor.plugin.export

fun main() = editor(format = EditorFormat.HD_SQUARE) {
    install(ExportImagePlugin())

    reset {
        with(export.compositionDrawer) {
            root.children.clear()
            pushModel()
            stroke = null
            fill = ColorRGBa.BLACK
            translate(width * 0.5, height * 0.5)
        }

        // drawing

        with(export.compositionDrawer) {
            popModel()
        }
    }

    draw {
        drawer.isolated {
            clear(ColorRGBa.WHITE)
            composition(export.compositionDrawer.composition)
        }
    }
}
