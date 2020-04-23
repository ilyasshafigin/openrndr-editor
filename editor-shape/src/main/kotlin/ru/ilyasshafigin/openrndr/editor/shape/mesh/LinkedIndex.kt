package ru.ilyasshafigin.openrndr.editor.shape.mesh

internal class LinkedIndex {

    private val links: MutableList<Int> = mutableListOf()

    fun linkTo(i: Int) {
        links += i
    }

    fun linked(i: Int): Boolean = links.any { j -> j == i }

    fun links(): List<Int> = links

    fun link(i: Int): Int = links[i]

    fun linkCount(): Int = links.size
}
