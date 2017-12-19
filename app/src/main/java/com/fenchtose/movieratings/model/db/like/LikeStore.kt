package com.fenchtose.movieratings.model.db.like

import com.fenchtose.movieratings.model.db.UserPreferneceApplier

interface LikeStore: UserPreferneceApplier {
    fun isLiked(imdbId: String): Boolean
    fun setLiked(imdbId: String, liked: Boolean)
}