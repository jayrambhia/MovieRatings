package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.model.Movie

interface SearchPage {
    fun showLoading(status: Boolean)
    fun setData(movies: ArrayList<Movie>)
    fun clearData()
}