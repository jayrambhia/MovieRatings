package com.fenchtose.movieratings.features.likespage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.model.entity.Movie

interface LikesPage: BaseMovieListPage {
    fun showRemoved(movies: List<Movie>, movie: Movie, index: Int)
    fun showAdded(movies: List<Movie>, movie: Movie, index: Int)
}