package com.fenchtose.movieratings.model.entity

enum class Sort {
    ALPHABETICAL,
    YEAR
}

fun List<Movie>.sort(sort: Sort): List<Movie> {
    return when(sort) {
        Sort.ALPHABETICAL -> sortedBy { it.title }
        Sort.YEAR -> sortedByDescending { it.year }
    }
}