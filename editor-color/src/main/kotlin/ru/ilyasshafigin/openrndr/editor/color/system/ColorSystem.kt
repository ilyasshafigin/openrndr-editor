package ru.ilyasshafigin.openrndr.editor.color.system

import org.openrndr.color.ColorRGBa

typealias ComponentType = Int

/**
 * Интерфейс палитры
 */
interface ColorSystem {

    /**
     * Возвращает объект цвета палитры
     * @param color оригинальный цвет в RGBA
     * @return объект цвета палитры
     */
    fun getColor(color: ColorRGBa): Color

    /**
     * Возвращает RGBA цвет компонента палитры
     * @param type тип компонента
     * @return RGBA цвет компонента палитры указанного типа
     */
    fun getComponentColorByType(type: ComponentType): ColorRGBa

    /**
     * Возвращает случайный тип компонента палитры
     * @return случайный тип компонента палитры
     */
    fun getRandomComponentType(): ComponentType

    /**
     * Интерфейс цвета палитры
     */
    interface Color {

        /**
         *
         *
         */
        val brightness: Double

        /**
         * Возвращает значение указанного компонента цветазначение указанного компонента цвета
         * @param type тип компонента палитры
         * @return значение указанного компонента цвета от 0 до 1
         */
        fun getComponentByType(type: ComponentType): Double
    }
}
