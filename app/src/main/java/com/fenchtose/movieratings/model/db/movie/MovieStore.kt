package com.fenchtose.movieratings.model.db.movie

import com.google.gson.JsonArray
import io.reactivex.Observable

interface MovieStore {
    fun export(): Observable<JsonArray>
}