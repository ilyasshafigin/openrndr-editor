package ru.ilyasshafigin.openrndr.editor.color

import org.openrndr.color.ColorRGBa

fun ColorRGBa.toHex(): Int {
    return ((a * 255).toInt() shl 24) or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
}

fun ColorRGBa.toHexString(): String {
    return StringBuilder("#")
        .append(a.toHexString())
        .append(r.toHexString())
        .append(g.toHexString())
        .append(b.toHexString())
        .toString()
}

private fun Double.toHexString(): String {
    var n = ((this * 255).toInt() and 255).toString(16)
    if (n.length < 2) {
        n = "0$n"
    }
    return n.toUpperCase()
}
