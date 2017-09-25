package io.github.chrislo27.rhre3.editor.picker


abstract class ScrollList<T> {

    val list: MutableList<T> = mutableListOf()
    val isEmpty: Boolean get() = list.isEmpty()

    var currentIndex: Int = 0
        set(value) {
            field = value.coerceIn(0, maxIndex)
        }
    val maxIndex: Int get() = (list.size - 1).coerceAtLeast(0)
    val current: T get() = list[currentIndex]

    var scroll: Int = 0
        set(value) {
            field = value.coerceIn(0, maxScroll)
        }
    abstract val maxScroll: Int

}