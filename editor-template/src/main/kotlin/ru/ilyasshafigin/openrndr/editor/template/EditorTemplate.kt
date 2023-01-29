package ru.ilyasshafigin.openrndr.editor.template

import org.openrndr.color.ColorRGBa
import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorConfig
import ru.ilyasshafigin.openrndr.editor.EditorFormat
import ru.ilyasshafigin.openrndr.editor.editor
import ru.ilyasshafigin.openrndr.editor.plugin.ExportImagePlugin
import ru.ilyasshafigin.openrndr.editor.plugin.SourcePlugin
import ru.ilyasshafigin.openrndr.editor.plugin.export
import ru.ilyasshafigin.openrndr.editor.plugin.source

private class TemplateEditor(config: EditorConfig) : Editor<Settings>(config) {

    override val settings = Settings()

    override suspend fun setup() {
        install(SourcePlugin())
        install(ExportImagePlugin())
    }

    override fun reset() {
        canvas.draw(drawer) {
            if (source.isSelected) {
                clear(source.image.get(0, 0))
            } else {
                clear(ColorRGBa.WHITE)
            }
        }
    }

    override fun draw() {
        if (!source.isSelected) return

        if (export.isRecord) {
            export.compositionDrawer.root.children.clear()
        }

        canvas.draw(drawer) {
            translate(source.area.corner)

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

fun main() = editor(
    EditorConfig(
        version = "0.1.0",
        format = EditorFormat.HD
    ),
    ::TemplateEditor
)
