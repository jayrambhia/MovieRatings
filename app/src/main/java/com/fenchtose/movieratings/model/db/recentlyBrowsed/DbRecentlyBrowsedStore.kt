package com.fenchtose.movieratings.model.db.recentlyBrowsed

import com.fenchtose.movieratings.model.RecentlyBrowsed
import com.fenchtose.movieratings.model.db.dao.RecentlyBrowsedDao
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class DbRecentlyBrowsedStore(private val dao: RecentlyBrowsedDao): RecentlyBrowsedStore {

    override fun update(data: RecentlyBrowsed) {
        Observable.just(data)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    dao.insert(it)
                }
    }
}