package com.fenchtose.movieratings.model.db

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Movie

interface UserPreferneceApplier {
    @WorkerThread
    fun apply(movie: Movie)
}