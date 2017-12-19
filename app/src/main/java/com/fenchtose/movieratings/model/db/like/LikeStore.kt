package com.fenchtose.movieratings.model.db.like

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.db.UserPreferneceApplier

interface LikeStore: UserPreferneceApplier {
    @WorkerThread
    fun isLiked(imdbId: String): Boolean
    fun setLiked(imdbId: String, liked: Boolean)
}