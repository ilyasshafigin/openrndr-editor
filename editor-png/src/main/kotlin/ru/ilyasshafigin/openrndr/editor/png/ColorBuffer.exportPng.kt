package ru.ilyasshafigin.openrndr.editor.png

import org.openrndr.draw.ColorBuffer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun ColorBuffer.exportPng(
    sketchName: String,
    imageName: String = sketchName,
    folderPath: String = "export/${sketchName.lowercase()}/png",
    timestamp: Long = System.currentTimeMillis(),
    fileDatePattern: String = "YYYYMMddHHmmss",
    async: Boolean = false
) {
    val fileDateFormat = SimpleDateFormat(fileDatePattern, Locale.getDefault())
    val directory = File(folderPath)
    if (!directory.exists()) directory.mkdirs()
    val fileName = "$imageName-${fileDateFormat.format(Date(timestamp))}.png"

    saveToFile(File(directory, fileName), async = async)
}
