package com.fenchtose.movieratings.features.likespage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.model.Movie

interface LikesPage: BaseMovieListPage {
    fun showRemoved(movie: Movie, index: Int)
    fun showAdded(movie: Movie, index: Int)
}