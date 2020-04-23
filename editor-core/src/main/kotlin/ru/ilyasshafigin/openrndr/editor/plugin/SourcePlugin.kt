package ru.ilyasshafigin.openrndr.editor.plugin

import ru.ilyasshafigin.openrndr.editor.Editor
import ru.ilyasshafigin.openrndr.editor.EditorPlugin
import ru.ilyasshafigin.openrndr.editor.image.Image
import ru.ilyasshafigin.openrndr.editor.image.convertToRGBa
import org.openrndr.dialogs.openFileDialog
import org.openrndr.draw.Drawer
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.loadImage
import org.openrndr.events.Event
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.shape.Rectangle
import java.io.File

class SourcePlugin : EditorPlugin {

    override val settings = @Description(title = "Source image") object {

        @ActionParameter("Select image")
        fun select() = performSelectImage()
    }

    private lateinit var _image: Image
    private lateinit var _area: Rectangle
    private lateinit var _fileName: String
    private var _isSelected: Boolean = false
    private lateinit var _editor: Editor<*>

    /** Source image */
    val image: Image get() = _image

    /** Name of selected image */
    val fileName: String get() = _fileName

    /** Area of selected image (position and size) */
    val area: Rectangle get() = _area

    /** Таймштамп загрузки исходного изображения */
    var timestamp: Long = System.currentTimeMillis()

    /** Флаг, выбрано и заглужено ли изображение */
    val isSelected: Boolean get() = _isSelected

    /** Image selection event */
    val selected = Event<ImageEvent>("editor-source-selected").postpone(true)

    override fun setup(editor: Editor<*>) {
        _editor = editor
        _area = Rectangle(0.0, 0.0, editor.width.toDouble(), editor.height.toDouble())
        _fileName = editor.name
        _image = Image(colorBuffer(editor.width, editor.height))

        selected.listen {
            _isSelected = true
            editor.performReset()
        }
    }

    override fun reset(editor: Editor<*>) {
        timestamp = System.currentTimeMillis()
    }

    override fun beforeDraw(drawer: Drawer, editor: Editor<*>) {
        selected.deliver()
    }

    internal fun performSelectImage() {
        openFileDialog(function = { file -> onFileSelected(file) })
    }

    private fun onFileSelected(selection: File) {
        val path = selection.path
        if (path.toLowerCase().matches("^.*?\\.(gif|jpg|tga|png|tif)$".toRegex())) {
            val loadedImage = Image(loadImage(selection).convertToRGBa())
            onImageLoaded(loadedImage, selection.name)
        } else {
            return
        }
    }

    private fun onImageLoaded(loadedImage: Image, imageName: String) {
        _image.destroy()
        _fileName = imageName.toLowerCase().replaceFirst("(?i)\\.(gif|jpg|tga|png|tif)".toRegex(), "")
        _area = if (_editor.height * loadedImage.width > _editor.width * loadedImage.height) {
            val h = _editor.width.toDouble() * loadedImage.height / loadedImage.width
            Rectangle(0.0, (_editor.height - h) * 0.5, _editor.width.toDouble(), h)
        } else {
            val w = _editor.height.toDouble() * loadedImage.width / loadedImage.height
            Rectangle((_editor.width - w) * 0.5, 0.0, w, _editor.height.toDouble())
        }
        _image = loadedImage.resized(_area.width.toInt(), _area.height.toInt())
        _image.loadPixels()
        loadedImage.destroy()
        performImageSelected()
    }

    private fun performImageSelected() {
        selected.trigger(ImageEvent())
    }
}

class ImageEvent

val Editor<*>.source: SourcePlugin
    get() = getPlugin()
