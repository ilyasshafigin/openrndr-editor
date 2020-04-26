package ru.ilyasshafigin.openrndr.editor.svg

import org.openrndr.shape.Composition
import org.openrndr.svg.saveToFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun Composition.exportSvg(
    sketchName: String,
    imageName: String = sketchName,
    folderPath: String = "export/${sketchName.toLowerCase()}/svg",
    timestamp: Long = System.currentTimeMillis(),
    fileDatePattern: String = "YYYYMMddHHmmss"
) {
    val fileDateFormat = SimpleDateFormat(fileDatePattern, Locale.getDefault())
    val directory = File(folderPath)
    if (!directory.exists()) directory.mkdirs()
    val fileName = "$imageName-${fileDateFormat.format(Date(timestamp))}.svg"

    saveToFile(File(directory, fileName))
}
