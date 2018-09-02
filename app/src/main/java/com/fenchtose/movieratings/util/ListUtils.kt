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

fun <T> List<T>.add(t: T, index: Int, strict: Boolean = true): List<T> {
    if (index < 0 || index >= size) {
        if (index > size && strict) {
            throw IndexOutOfBoundsException("Invalid index: $index provided. list size is $size")
        }

        return toMutableList().apply { add(t) }
    }

    return toMutableList().apply { add(index, t) }
}