package ru.ilyasshafigin.openrndr.editor

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2

class EditorBuilder<S : EditorSettings>(
    val config: EditorConfig
) {

    private var setupFun: suspend Editor<S>.() -> Unit = {}
    private var drawFun: Editor<S>.(Drawer) -> Unit = {}
    private var resetFun: Editor<S>.() -> Unit = {}
    private var editor: Editor<S>? = null

    val width: Int get() = editor!!.width
    val height: Int get() = editor!!.height
    val center: Vector2 get() = editor!!.center
    val resolution: Vector2 get() = editor!!.resolution
    val aspectRatio: Double get() = editor!!.aspectRatio

    fun setup(init: suspend Editor<S>.() -> Unit) {
        setupFun = init
    }

    fun reset(init: Editor<S>.() -> Unit) {
        resetFun = init
    }

    fun draw(init: Editor<S>.(drawer: Drawer) -> Unit) {
        drawFun = init
    }

    fun <T : EditorPlugin> install(plugin: T): T {
        return checkNotNull(editor).install(plugin)
    }

    fun <T : EditorPlugin> install(plugin: T, init: T.() -> Unit): T {
        plugin.init()
        return install(plugin)
    }

    fun build(
        config: EditorConfig,
        settings: S,
        init: EditorBuilder<S>.() -> Unit
    ): Editor<S> = object : Editor<S>(config) {

        override val settings: S = settings

        override suspend fun setup() {
            init()
            setupFun(this)
        }

        override fun reset() {
            resetFun(this)
        }

        override fun draw() {
            drawFun(this, drawer)
        }
    }.also { editor = it }
}
