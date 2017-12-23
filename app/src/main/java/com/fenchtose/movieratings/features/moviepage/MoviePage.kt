package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.model.Movie

interface MoviePage {
    fun loadImage(poster: String)
    fun showMovie(movie: Movie)
    fun showLoading()
    fun showError()
}