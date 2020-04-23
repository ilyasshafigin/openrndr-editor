package ru.ilyasshafigin.openrndr.editor.png

import org.openrndr.draw.ColorBuffer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val fileDateFormat = SimpleDateFormat("YYYYMMddHHmmss", Locale.getDefault())
private const val exportFolder = "export"
private const val pngFolder = "png"

fun ColorBuffer.exportPng(
    sketchName: String,
    imageName: String = "",
    timestamp: Long = System.currentTimeMillis()
) {
    val exportDirectory = File(exportFolder)
    if (!exportDirectory.exists()) exportDirectory.mkdir()
    val sketchDirectory = File(exportDirectory, sketchName.toLowerCase())
    if (!sketchDirectory.exists()) sketchDirectory.mkdir()
    val pngDirectory = File(sketchDirectory, pngFolder)
    if (!pngDirectory.exists()) pngDirectory.mkdir()

    val fileName = "$imageName${if (imageName.isEmpty()) "" else "-"}$sketchName-${fileDateFormat.format(Date(timestamp))}.png"
    saveToFile(File(pngDirectory, fileName), async = true)
}

