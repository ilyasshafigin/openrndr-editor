package ru.ilyasshafigin.openrndr.editor.template

import org.openrndr.color.ColorRGBa
import ru.ilyasshafigin.openrndr.editor.editor
import ru.ilyasshafigin.openrndr.editor.plugin.ExportImagePlugin
import ru.ilyasshafigin.openrndr.editor.plugin.SourcePlugin
import ru.ilyasshafigin.openrndr.editor.plugin.export
import ru.ilyasshafigin.openrndr.editor.plugin.source

fun main() = editor(
    version = "0.1.0",
    settings = Settings()
) {
    install(SourcePlugin())
    install(ExportImagePlugin())

    reset {
        canvas.draw(drawer) {
            if (source.isSelected) {
                clear(source.image.get(0, 0))
            } else {
                clear(ColorRGBa.WHITE)
            }
        }
    }

    draw { drawer ->
        if (!source.isSelected) return@draw

        if (export.isRecord) {
            export.compositionDrawer.root.children.clear()
        }

        canvas.draw(drawer) {
            if (settings.isDrawing) {
                if (export.isRecord) {
                    with(export.compositionDrawer) {
                        pushModel()
                        strokeWeight = 1.0
                        stroke = ColorRGBa.BLACK
                        fill = null
                        translate(source.area.corner)
                    }
                }

                // drawing

                if (export.isRecord) {
                    export.compositionDrawer.popModel()
                }
            }
        }

        if (export.isRecord) {
            export.exportSvg()
            export.exportGcode()
            export.isRecord = false
            performReset()
        }
    }
}
