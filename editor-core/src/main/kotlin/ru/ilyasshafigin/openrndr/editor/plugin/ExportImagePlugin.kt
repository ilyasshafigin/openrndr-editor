package ru.ilyasshafigin.openrndr.editor.plugin

import ru.ilyasshafigin.openrndr.editor.Canvas
import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorPlugin
import ru.ilyasshafigin.openrndr.editor.gcode.exportGcode
import ru.ilyasshafigin.openrndr.editor.png.exportPng
import ru.ilyasshafigin.openrndr.editor.shape.flatten
import ru.ilyasshafigin.openrndr.editor.svg.exportSvg
import mu.KotlinLogging
import org.openrndr.draw.Drawer
import org.openrndr.events.Event
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.shape.CompositionDrawer

class ExportImagePlugin : EditorPlugin {

    override val settings = @Description(title = "Export") object {

        @ActionParameter("PNG", order = 1)
        fun exportPng() = performExportPng()

        @ActionParameter("SVG", order = 2)
        fun exportSvg() = performExportSvg()
    }

    /** */
    val compositionDrawer = CompositionDrawer()

    /** Флаги записи для экспорта в SVG */
    var isRecord: Boolean = false

    /** Флаг сохранения обработанного изображения */
    private var isSaveFrame: Boolean = false

    val png = Event<ExportEvent>("editor-export-png").postpone(true)
    val svg = Event<ExportEvent>("editor-export-svg").postpone(true)

    private val logger = KotlinLogging.logger {}

    private lateinit var editor: Editor<*>
    private lateinit var canvas: Canvas
    private lateinit var sourceFileName: () -> String
    private lateinit var sourceTimeStamp: () -> Long

    override fun setup(editor: Editor<*>) {
        this.editor = editor
        this.canvas = editor.canvas

        val timeStamp = System.currentTimeMillis()

        sourceFileName = {
            if (editor.isPluginInstalled<SourcePlugin>()) {
                editor.source.fileName
            } else {
                editor.name
            }
        }
        sourceTimeStamp = {
            if (editor.isPluginInstalled<SourcePlugin>()) {
                editor.source.timestamp
            } else {
                timeStamp
            }
        }

        svg.listen { editor.performReset() }
    }

    override fun reset(editor: Editor<*>) {
        compositionDrawer.root.children.clear()
    }

    override fun beforeDraw(drawer: Drawer, editor: Editor<*>) {
        png.deliver()
        svg.deliver()
    }

    override fun afterDraw(drawer: Drawer, editor: Editor<*>) {
        if (isSaveFrame) {
            exportPng(sourceFileName())
            isSaveFrame = false
        }
    }

    fun performExportPng() {
        isSaveFrame = true
        png.trigger(ExportEvent())
    }

    fun performExportSvg() {
        isRecord = true
        svg.trigger(ExportEvent())
    }

    /**
     * Сохраняет нарисованное изображение в PNG
     */
    fun exportPng(overrideImageName: String? = null) {
        logger.debug { "Begin export PNG image '${overrideImageName ?: sourceFileName()}'" }
        canvas.colorBuffer.exportPng(editor.name, overrideImageName ?: sourceFileName())
    }

    /**
     * Экспортирует `Composition`, нарисованный с помощью [compositionDrawer] в SVG файл
     */
    fun exportSvg(overrideImageName: String? = null) {
        logger.debug { "Begin flatten shapes in composition" }

        val composition = compositionDrawer.composition.flatten()

        logger.debug { "Begin export to SVG image '${overrideImageName ?: sourceFileName()}'" }

        composition.exportSvg(
            sketchName = editor.name,
            imageName = overrideImageName ?: sourceFileName(),
            timestamp = sourceTimeStamp()
        )
    }

    /**
     * Экспортирует `Composition`, нарисованный с помощью [compositionDrawer] в GCODE файл
     */
    fun exportGcode(overrideImageName: String? = null) {
        logger.debug { "Begin export vector image '${overrideImageName ?: sourceFileName()}'" }

        compositionDrawer.composition.exportGcode(
            sketchName = editor.name,
            imageName = overrideImageName ?: sourceFileName(),
            timestamp = sourceTimeStamp(),
            width = canvas.width,
            height = canvas.height
        )
    }
}

class ExportEvent

val Editor<*>.export: ExportImagePlugin
    get() = getPlugin()
