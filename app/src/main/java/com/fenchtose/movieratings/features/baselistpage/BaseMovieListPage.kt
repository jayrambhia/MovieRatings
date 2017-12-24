package com.fenchtose.movieratings.features.baselistpage

import com.fenchtose.movieratings.model.Movie

interface BaseMovieListPage {
    fun setData(movies: ArrayList<Movie>)
}