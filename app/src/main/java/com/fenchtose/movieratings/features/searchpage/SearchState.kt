package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.model.Movie

data class SearchState(var query: String, var movies: ArrayList<Movie>, var page: Int): PresenterState