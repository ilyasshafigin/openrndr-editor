package ru.ilyasshafigin.openrndr.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import mu.KotlinLogging
import org.openrndr.Application
import org.openrndr.ApplicationMouse
import org.openrndr.Extension
import org.openrndr.KEY_ESCAPE
import org.openrndr.Keyboard
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.internal.Driver
import org.openrndr.launch
import org.openrndr.math.Vector2
import ru.ilyasshafigin.openrndr.editor.extension.ScreenOutput
import java.net.URL
import kotlin.coroutines.CoroutineContext

/**
 * General editor class.
 *
 * Lifecycle events:
 *  * [setup]
 *  * [draw]
 *  * [reset]
 */
@Suppress("LeakingThis")
abstract class Editor<S : EditorSettings>(
    /** Editor config */
    val config: EditorConfig
) {

    /**
     * Editor settings
     */
    abstract val settings: S

    val logger = KotlinLogging.logger {}
    val gui = GUI()
    val canvas = Canvas()

    val program = EditorProgram(this)

    val width: Int get() = program.width
    val height: Int get() = program.height
    val center: Vector2 by lazy { Vector2(width * 0.5, height * 0.5) }
    val resolution: Vector2 by lazy { Vector2(width * 1.0, height * 1.0) }

    val drawer: Drawer get() = program.drawer
    val driver: Driver get() = program.driver

    val application: Application get() = program.application

    val frameCount: Int get() = program.frameCount
    val seconds: Double get() = program.seconds
    val deltaTime: Double get() = program.deltaTime

    val clipboard: Program.Clipboard get() = program.clipboard
    val window: Program.Window get() = program.window
    val keyboard: Keyboard get() = program.keyboard
    val mouse: ApplicationMouse get() = program.mouse

    /**  */
    var name: String = config.name

    /** */
    val plugins = mutableListOf<EditorPlugin>()

    /** Флаг сброса редактора и очистки экрана */
    private var isReset = true

    private fun preSetup() {
        logger.info { "Pre setup editor: name = $name, config = $config" }

        keyboard.keyDown.listen { event ->
            if (event.key == KEY_ESCAPE) {
                application.exit()
            }
        }

        canvas.setup(this)
    }

    private fun postSetup() {
        logger.info { "Post setup editor" }

        program.extend(gui) {
            compartmentsCollapsedByDefault = true
            doubleBind = true

            add(
                @Description(title = "Canvas")
                object {
                    @ActionParameter("Reset and clear")
                    fun reset() = performReset()
                }
            )

            plugins.forEach { plugin ->
                plugin.settings?.let { add(it) }
            }

            add(settings)
        }
        program.extend(Plugins(this))
        program.extend(ScreenOutput(canvas.colorBuffer))
    }

    private fun preDraw() {
        if (isReset) {
            plugins.forEach { plugin -> plugin.reset(this) }
            reset()
            isReset = false
        }
    }

    private fun postDraw() {
    }

    /**
     * This is run exactly once before the first call to draw()
     */
    open fun setup() {
    }

    /**
     * This is run exactly once before the call to [draw], when a reset is performed
     */
    open fun reset() {
    }

    /**
     * This is the user facing draw call
     */
    open fun draw() {
    }

    /**
     * Perform reset
     */
    fun performReset() {
        logger.info { "Reset is performed" }
        isReset = true
    }

    /**
     * Install plugin [plugin] into current editor
     */
    fun <T : EditorPlugin> install(plugin: T): T {
        logger.info { "Setup plugin: $plugin" }

        plugins += plugin
        plugin.setup(this)
        return plugin
    }

    /**
     * Install plugin [plugin] and configure it
     */
    fun <T : EditorPlugin> install(plugin: T, configure: T.() -> Unit): T {
        plugin.configure()
        return install(plugin)
    }

    inline fun <reified T : EditorPlugin> isPluginInstalled(): Boolean {
        return plugins.any { it is T }
    }

    inline fun <reified T : EditorPlugin> getPlugin(): T {
        val plugin = plugins.find { it is T } as? T
        return plugin ?: throw IllegalStateException("'${T::class.simpleName}' is not installed")
    }

    class EditorProgram(private val editor: Editor<*>) : Program() {

        override suspend fun setup() {
            editor.preSetup()
            editor.setup()
            editor.postSetup()
        }

        override fun draw() {
            editor.preDraw()
            editor.draw()
            editor.postDraw()
        }
    }

    private class Plugins(private val editor: Editor<*>) : Extension {

        override var enabled: Boolean = true

        override fun beforeDraw(drawer: Drawer, program: Program) {
            editor.plugins.forEach { plugin ->
                plugin.beforeDraw(drawer, editor)
            }
        }

        override fun afterDraw(drawer: Drawer, program: Program) {
            editor.plugins.reversed().forEach { plugin ->
                plugin.afterDraw(drawer, editor)
            }
        }

        override fun shutdown(program: Program) {
            editor.plugins.forEach { plugin ->
                plugin.shutdown(editor)
            }
        }
    }
}

fun Editor<*>.launch(
    context: CoroutineContext = program.dispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = program.launch(context, start, block)

/**
 * Resolves resource named [name] relative to [clazz] as a [String] based URL.
 */
fun resourceUrl(name: String, clazz: Class<*> = Editor::class.java): String {
    var resource: URL? = clazz.getResource(name)
    return if (resource == null) {
        resource = clazz.classLoader.getResource(name)
        if (resource == null) {
            return name
        } else {
            resource.toExternalForm()
        }
    } else {
        resource.toExternalForm()
    }
}

/**
 * Resolves resource named [name] relative to [clazz] as a [String] based URL.
 */
fun Editor<*>.resourceUrl(name: String, clazz: Class<*> = this::class.java): String {
    return resourceUrl(name, clazz)
}
