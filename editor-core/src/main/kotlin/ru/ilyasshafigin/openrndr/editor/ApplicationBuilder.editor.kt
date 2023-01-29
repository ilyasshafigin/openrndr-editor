package ru.ilyasshafigin.openrndr.editor

import org.openrndr.ApplicationBuilder
import org.openrndr.Fullscreen
import org.openrndr.application

fun editor(
    version: String = "1.0.0",
    format: EditorFormat = EditorFormat.HD,
    init: EditorBuilder<EditorSettings>.() -> Unit
) = editor(
    config = EditorConfig(
        version = version,
        format = format
    ),
    settings = object : EditorSettings {},
    init = init
)

fun <S : EditorSettings> editor(
    version: String = "1.0.0",
    format: EditorFormat = EditorFormat.HD,
    settings: S,
    init: EditorBuilder<S>.() -> Unit
) = editor(
    config = EditorConfig(
        version = version,
        format = format
    ),
    settings = settings,
    init = init
)

fun <S : EditorSettings> editor(
    config: EditorConfig = EditorConfig(),
    settings: S,
    init: EditorBuilder<S>.() -> Unit
) = application {
    configure(config)
    val builder = EditorBuilder<S>(config)
    program = builder.build(config, settings, init).program
}

fun <S : EditorSettings> editor(
    config: EditorConfig,
    editorProvider: (config: EditorConfig) -> Editor<S>
) = application {
    configure(config)
    program = editorProvider(config).program
}

fun ApplicationBuilder.configure(config: EditorConfig) {
    configure {
        title = "${config.name} ${config.version}"
        if (config.format == EditorFormat.FULLSCREEN) {
            fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        } else {
            width = (config.format.width * config.previewScale).toInt()
            height = (config.format.height * config.previewScale).toInt()
        }
    }
}
