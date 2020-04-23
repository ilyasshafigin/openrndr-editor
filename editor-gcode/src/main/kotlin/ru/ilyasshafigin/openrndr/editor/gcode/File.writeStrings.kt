package ru.ilyasshafigin.openrndr.editor.gcode

import java.io.File

internal fun File.writeStrings(data: List<String>) = printWriter().use { writer ->
    data.forEach(writer::println)
    writer.flush()
}

