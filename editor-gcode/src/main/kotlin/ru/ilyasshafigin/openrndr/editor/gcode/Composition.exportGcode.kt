package ru.ilyasshafigin.openrndr.editor.gcode

import org.openrndr.math.Vector2
import org.openrndr.shape.Composition
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val penUp = "M05"
private const val penDown = "M03"

private const val travelSpeed = 16000
private const val penSpeed = 5000
private const val power = 275
private const val powerDelay = 0.2 // seconds

private val Double.mm get() = this / 3.5433070866
private val Double.str get() = "%.3f".format(this)

fun Composition.exportGcode(
    sketchName: String,
    imageName: String = sketchName,
    folderPath: String = "export/${sketchName.toLowerCase()}/gcode",
    timestamp: Long = System.currentTimeMillis(),
    fileDatePattern: String = "YYYYMMddHHmmss",
    width: Int,
    height: Int
) {
    val fileDateFormat = SimpleDateFormat(fileDatePattern, Locale.getDefault())
    val directory = File(folderPath)
    if (!directory.exists()) directory.mkdirs()
    val fileName = "$imageName-${fileDateFormat.format(Date(timestamp))}.nc"
    val file = File(directory, fileName)

    file.writeStrings(writeGcode(this, width, height))
}

fun writeGcode(composition: Composition, width: Int, height: Int): List<String> {
    val cmm = Vector2(width * 0.5.mm, height * 0.5.mm)
    val commands = mutableListOf<String>()

    commands.clear()
    commands += "$penUp S0"
    commands += "G90"
    //commands.add("G1Z0");
    commands += "G21" // in mm
    commands += "G1 F$travelSpeed"

    composition.findShapes()
        .map { it.flatten() }
        .forEach { node ->
            node.shape.contours.forEach { contour ->
                contour.segments.forEachIndexed { index, segment ->
                    if (index == 0) {
                        val x = segment.start.x.mm - cmm.x
                        val y = segment.start.y.mm - cmm.y
                        commands += "G1 X${x.str} Y${y.str} F$penSpeed"

                        commands += "G4 P0"
                        commands += penDown
                        commands += "S$power"
                        commands += "G4 P${powerDelay.str}"
                    }

                    commands += when (segment.control.size) {
//                      1 -> "Q${segment.control[0].x}, ${segment.control[0].y}, ${segment.end.x}, ${segment.end.y}"
//                      2 -> "C${segment.control[0].x}, ${segment.control[0].y}, ${segment.control[1].x}, ${segment.control[1].y}, ${segment.end.x}, ${segment.end.y}"
                        else -> {
                            val x = segment.end.x.mm - cmm.x
                            val y = segment.end.y.mm - cmm.y
                            "G1 X${x.str} Y${y.str} F$penSpeed"
                        }
                    }
                }
            }

            commands += "G4 P0"
            commands += penUp
            commands += "S0"
        }

    commands += "G1 X0 Y0 F$travelSpeed"
    commands += "M30"

    return commands
}
