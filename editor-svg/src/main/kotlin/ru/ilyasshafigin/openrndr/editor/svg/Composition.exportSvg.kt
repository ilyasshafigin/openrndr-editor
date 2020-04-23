package ru.ilyasshafigin.openrndr.editor.svg

import org.openrndr.shape.Composition
import org.openrndr.svg.saveToFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val fileDateFormat = SimpleDateFormat("YYYYMMddHHmmss", Locale.getDefault())
private const val exportFolder = "export"
private const val svgFolder = "svg"

fun Composition.exportSvg(
    sketchName: String,
    imageName: String = "",
    timestamp: Long = System.currentTimeMillis()
) {
    val exportDirectory = File(exportFolder)
    if (!exportDirectory.exists()) exportDirectory.mkdir()
    val sketchDirectory = File(exportDirectory, sketchName.toLowerCase())
    if (!sketchDirectory.exists()) sketchDirectory.mkdir()
    val svgDirectory = File(sketchDirectory, svgFolder)
    if (!svgDirectory.exists()) svgDirectory.mkdir()

    val fileName = "$imageName${if (imageName.isEmpty()) "" else "-"}$sketchName-${fileDateFormat.format(Date(timestamp))}.svg"
    saveToFile(File(svgDirectory, fileName))
}
