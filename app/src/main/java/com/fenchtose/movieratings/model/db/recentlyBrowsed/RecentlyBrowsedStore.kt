package com.fenchtose.movieratings.model.db.recentlyBrowsed

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.entity.RecentlyBrowsed
import io.reactivex.Observable
import io.reactivex.Single

interface RecentlyBrowsedStore {
    fun update(data: RecentlyBrowsed)
    fun deleteAll(): Observable<Int>
    fun export(): Single<List<RecentlyBrowsed>>
    @WorkerThread
    fun import(history: List<RecentlyBrowsed>): Int
}