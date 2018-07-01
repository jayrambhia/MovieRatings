package com.fenchtose.movieratings.model.db.displayedRatings

import com.fenchtose.movieratings.model.entity.DisplayedRating
import io.reactivex.Observable

interface DisplayedRatingsStore {
    fun update(rating: DisplayedRating)
    fun deleteAll(): Observable<Int>
    fun getUniqueRatingsCount(): Observable<Int>
    fun getRatingsCount(): Observable<Int>
}