package ru.ilyasshafigin.openrndr.editor.template

import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import ru.ilyasshafigin.openrndr.editor.EditorSettings

@Description(title = "Settings")
internal class Settings : EditorSettings {

    @BooleanParameter("Drawing", order = 10)
    var isDrawing = true
    @DoubleParameter("Particle alpha", 0.0, 1.0, 2, order = 20)
    var particleAlpha = 1.0
    @IntParameter("Particle count", 0, 1000, order = 30)
    var particleCount = 100
}
