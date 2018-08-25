package com.fenchtose.movieratings.util

fun <T> List<T>.add(t: T): List<T> {
    return toMutableList().apply { add(t) }
}