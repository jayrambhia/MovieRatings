package com.fenchtose.movieratings.model.db.recentlyBrowsed

import androidx.annotation.WorkerThread
import com.fenchtose.movieratings.model.db.entity.RecentlyBrowsed
import com.fenchtose.movieratings.model.db.dao.RecentlyBrowsedDao
import io.reactivex.Observable
import io.reactivex.Single
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

    override fun export(): Single<List<RecentlyBrowsed>> {
        return Single.defer {
            Single.fromCallable {
                dao.getAll()
            }
        }
    }

    @WorkerThread
    override fun import(history: List<RecentlyBrowsed>): Int {
        return dao.importData(history).filter { it != -1L }.count()
    }
}