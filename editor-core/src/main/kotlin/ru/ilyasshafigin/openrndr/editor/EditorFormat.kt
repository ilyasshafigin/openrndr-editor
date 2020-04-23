package ru.ilyasshafigin.openrndr.editor

enum class EditorFormat(val width: Int, val height: Int) {
    FULLSCREEN(-1, -1),
    HD(1280, 720),
    HD_PORTRAIT(720, 1280),
    HD_SQUARE(720, 720),
    FULL_HD(1920, 1080),
    FULL_HD_PORTRAIT(1080, 1920),
    FULL_HD_SQUARE(1080, 1080),
    ULTRA_HD(3840, 2160),
    ULTRA_HD_PORTRAIT(2160, 3840),
    ULTRA_HD_SQUARE(2160, 2160),
    INSTAGRAM_SQUARE(1080, 1080),
    INSTAGRAM_4_5(1080, 1350)
}
