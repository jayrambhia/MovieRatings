package com.fenchtose.movieratings.model.db

import androidx.annotation.WorkerThread
import com.fenchtose.movieratings.model.entity.Movie

interface UserPreferenceApplier {
    @WorkerThread
    fun apply(movie: Movie): Movie
}

fun Collection<UserPreferenceApplier>.apply(movie: Movie): Movie {
    var updated = movie
    forEach {
        updated = it.apply(updated)
    }

    return updated
}