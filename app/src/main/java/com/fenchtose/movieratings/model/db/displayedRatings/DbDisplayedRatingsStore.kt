package com.fenchtose.movieratings.model.db.displayedRatings

import android.annotation.SuppressLint
import com.fenchtose.movieratings.model.db.dao.DisplayedRatingDao
import com.fenchtose.movieratings.model.db.entity.DisplayedRating
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class DbDisplayedRatingsStore private constructor(private val dao: DisplayedRatingDao): DisplayedRatingsStore {

    companion object {
        private var instance: DbDisplayedRatingsStore? = null

        fun getInstance(dao: DisplayedRatingDao): DisplayedRatingsStore {
            if (instance == null) {
                instance = DbDisplayedRatingsStore(dao)
            }

            return instance!!
        }
    }

    @SuppressLint("CheckResult")
    override fun update(rating: DisplayedRating) {
        Observable.just(rating)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    dao.insert(it)
                }
    }

    override fun deleteAll(): Observable<Int> {
        return Observable.defer { Observable.just(dao.deleteAll()) }
    }

    override fun getUniqueRatingsCount(): Observable<Int> {
        return Observable.defer { Observable.just(dao.countUnique()) }
    }

    override fun getRatingsCount(): Observable<Int> {
        return Observable.defer { Observable.just(dao.countAll()) }
    }
}