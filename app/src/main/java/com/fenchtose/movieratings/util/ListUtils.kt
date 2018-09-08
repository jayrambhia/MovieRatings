package com.fenchtose.movieratings.util

fun<T> List<T>.addAll(collection: Collection<T>): List<T> {
    val data = ArrayList<T>(this)
    data.addAll(collection)
    return data
}

fun <T> List<T>.replace(index: Int, t: T): List<T> {
    if (index < 0 || index >= size) {
        throw IndexOutOfBoundsException("Invalid index: $index provided. list size is $size")
    }

    val data = ArrayList(this)
    data.removeAt(index)
    data.add(index, t)

    return data
}

fun <T> List<T>.add(t: T): List<T> {
    return toMutableList().apply { add(t) }
}

fun <T> List<T>.push(t: T): List<T> {
    return add(t)
}

fun <T> List<T>.pop(): List<T> {
    return toMutableList().apply { removeAt(size - 1) }
}

fun <T> ArrayList<T>.swapIfUpdated(t: T, index: Int) {
    if (t != get(index)) {
        removeAt(index)
        add(index, t)
    }
}

fun <T> List<T>.swapLastIfUpdated(t: T): List<T> {
    if (t != last()) {
        return toMutableList().apply {
            removeAt(size - 1)
            add(t)
        }
    }

    return this
}

fun <T> List<T>.add(t: T, index: Int, strict: Boolean = true): List<T> {
    if (index < 0 || index >= size) {
        if (index > size && strict) {
            throw IndexOutOfBoundsException("Invalid index: $index provided. list size is $size")
        }

        return toMutableList().apply { add(t) }
    }

    return toMutableList().apply { add(index, t) }
}