package com.fenchtose.movieratings.model.db.recentlyBrowsed

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.RecentlyBrowsed
import com.fenchtose.movieratings.model.db.dao.RecentlyBrowsedDao
import com.google.gson.JsonArray
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class DbRecentlyBrowsedStore private constructor(private val dao: RecentlyBrowsedDao): RecentlyBrowsedStore {

    companion object {
        private var instance: DbRecentlyBrowsedStore? = null

        fun getInstance(dao: RecentlyBrowsedDao): RecentlyBrowsedStore {
            if (instance == null) {
                instance = DbRecentlyBrowsedStore(dao)
            }

            return instance!!
        }
    }

    override fun update(data: RecentlyBrowsed) {
        Observable.just(data)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    dao.insert(it)
                }
    }

    override fun deleteAll(): Observable<Int> = Observable.defer {
        Observable.just(dao.deleteAll())
    }

    override fun export(): Observable<JsonArray> {
        return Observable.defer {
            Observable.fromCallable {
                dao.getAll()
            }.map {
                MovieRatingsApplication.gson.toJsonTree(it).asJsonArray
            }
        }
    }

    @WorkerThread
    override fun import(history: List<RecentlyBrowsed>): Int {
        return dao.importData(history).filter { it != -1L }.count()
    }
}