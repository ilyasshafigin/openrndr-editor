package ru.ilyasshafigin.openrndr.editor

import org.openrndr.extra.parameters.Description

/**
 * Editor settings.
 *
 * For example:
 * ```
 * class Settings : EditorSettings {
 *
 *     @BooleanParameter("Drawing")
 *     var isDrawing = true
 * }
 * ```
 */
@Description(title = "Settings")
interface EditorSettings
