package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.MovieCollection
import io.reactivex.Observable

interface MovieCollectionProvider {
    fun getCollections(): Observable<List<MovieCollection>>
}