package ru.ilyasshafigin.openrndr.editor

import org.openrndr.draw.Drawer

/**
 * Lifecycle events:
 *  * [setup]
 *  * [reset]
 *  * [beforeDraw]
 *  * [afterDraw]
 *  * [shutdown]
 */
interface EditorPlugin {

    /**
     * Plugin settings.
     *
     * For example:
     * ```
     * override val settings = object {
     *
     *     @ActionParameter("Action")
     *     fun action() {
     *         //....
     *     }
     * }
     * ```
     */
    val settings: Any?
        get() = null

    /**
     *
     */
    fun setup(editor: Editor<*>) {}

    /**
     *
     */
    fun reset(editor: Editor<*>) {}

    /**
     *
     */
    fun beforeDraw(drawer: Drawer, editor: Editor<*>) {}

    /**
     *
     */
    fun afterDraw(drawer: Drawer, editor: Editor<*>) {}

    /**
     * Shutdown is called when the host application is quit
     */
    fun shutdown(editor: Editor<*>) {}
}
