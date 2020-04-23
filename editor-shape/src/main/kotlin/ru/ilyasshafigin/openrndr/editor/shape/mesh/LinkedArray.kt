package ru.ilyasshafigin.openrndr.editor.shape.mesh

internal class LinkedArray(size: Int) {

    private val array: Array<LinkedIndex> = Array(size) { LinkedIndex() }

    operator fun get(i: Int): LinkedIndex = array[i]

    fun link(a: Int, b: Int) {
        array[a].linkTo(b)
        array[b].linkTo(a)
    }

    fun linked(a: Int, b: Int): Boolean = array[a].linked(b)
}
