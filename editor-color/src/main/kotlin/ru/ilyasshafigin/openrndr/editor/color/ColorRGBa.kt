package ru.ilyasshafigin.openrndr.editor.color

import org.openrndr.color.ColorRGBa

fun ColorRGBa.toHex(): Int {
    return ((a * 255).toInt() shl 24) or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
}
