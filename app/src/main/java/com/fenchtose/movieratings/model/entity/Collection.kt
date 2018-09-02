package com.fenchtose.movieratings.model.entity


data class MovieCollection(
    val id: Long,
    val name: String,
    val movies: List<Movie>
) {
    companion object {
        fun invalid(): MovieCollection {
            return MovieCollection(-1, "", listOf())
        }
    }
}

fun com.fenchtose.movieratings.model.db.entity.MovieCollection.convert(): MovieCollection {
    return MovieCollection(
            id = id,
            name = name,
            movies = listOf()
    )
}