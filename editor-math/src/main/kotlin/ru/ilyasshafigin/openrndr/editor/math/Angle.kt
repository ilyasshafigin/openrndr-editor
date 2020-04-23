package ru.ilyasshafigin.openrndr.editor.math

val Double.radians: Double get() = this * DEG_TO_RAD
val Double.degrees: Double get() = this * RAD_TO_DEG

val Int.radians: Double get() = this * DEG_TO_RAD
