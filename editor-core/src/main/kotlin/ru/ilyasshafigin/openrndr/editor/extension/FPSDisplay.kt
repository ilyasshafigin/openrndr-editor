package ru.ilyasshafigin.openrndr.editor.extension

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont

class FPSDisplay : Extension {

    override var enabled: Boolean = true

    private lateinit var font: FontImageMap

    override fun setup(program: Program) {
        font = loadFont("data/fonts/IBMPlexMono-Regular.ttf", 16.0)
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        drawer.isolated {
            fill = ColorRGBa.WHITE
            fontMap = font
            text("${(1.0 / program.deltaTime).toInt()} FPS", 10.0, 20.0)
        }
    }
}
