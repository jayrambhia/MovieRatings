package com.fenchtose.movieratings.features.search_page

import com.fenchtose.movieratings.model.Movie

interface SearchPage {
    fun showLoading(status: Boolean)
    fun setData(movies: ArrayList<Movie>)
    fun clearData()
}