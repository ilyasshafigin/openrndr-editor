package ru.ilyasshafigin.openrndr.editor

import org.openrndr.exceptions.stackRootClassName

data class EditorConfig(
    val name: String = stackRootClassName().split('.').last(),
    val version: String = "1.0.0",
    val format: EditorFormat = EditorFormat.HD,
    val previewScale: Double = when (format) {
        EditorFormat.HD_PORTRAIT -> 0.64
        EditorFormat.FULL_HD -> 0.66
        EditorFormat.FULL_HD_PORTRAIT -> 0.44
        EditorFormat.FULL_HD_SQUARE -> 0.72
        EditorFormat.ULTRA_HD -> 0.33
        EditorFormat.ULTRA_HD_PORTRAIT -> 0.22
        EditorFormat.ULTRA_HD_SQUARE -> 0.36
        EditorFormat.INSTAGRAM_4_5 -> 0.62
        EditorFormat.INSTAGRAM_SQUARE -> 0.78
        else -> 1.0
    }
)
