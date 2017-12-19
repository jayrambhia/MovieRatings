package com.fenchtose.movieratings.model.db

import com.fenchtose.movieratings.model.Movie

interface UserPreferneceApplier {
    fun apply(movie: Movie)
}