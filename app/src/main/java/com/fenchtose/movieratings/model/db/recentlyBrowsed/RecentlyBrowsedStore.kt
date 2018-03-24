package com.fenchtose.movieratings.model.db.recentlyBrowsed

import com.fenchtose.movieratings.model.RecentlyBrowsed
import io.reactivex.Observable

interface RecentlyBrowsedStore {
    fun update(data: RecentlyBrowsed)
    fun deleteAll(): Observable<Int>
}