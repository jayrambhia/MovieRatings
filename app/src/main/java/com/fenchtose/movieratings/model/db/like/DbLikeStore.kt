package com.fenchtose.movieratings.model.db.like

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.db.entity.Fav
import com.fenchtose.movieratings.model.db.dao.FavDao
import com.fenchtose.movieratings.model.entity.Movie
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class DbLikeStore private constructor(private val likeDao: FavDao) : LikeStore {

    companion object {
        private var instance: DbLikeStore? = null

        fun getInstance(likeDao: FavDao) :DbLikeStore {
            if (instance == null) {
                instance = DbLikeStore(likeDao)
            }

            return instance!!
        }
    }

    @WorkerThread
    override fun apply(movie: Movie): Movie {
        return movie.like(isLiked(movie.imdbId))
    }

    @WorkerThread
    override fun isLiked(imdbId: String): Boolean {
        val fav = likeDao.getFav(imdbId)
        fav?.let {
            return it.liked
        }

        return false
    }

    override fun setLiked(imdbId: String, liked: Boolean) {
        val fav = Fav()
        fav.id = imdbId
        fav.liked = liked
        Observable.just(fav)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    likeDao.insert(it)
                }
    }

    override fun deleteAll(): Observable<Int> {
        return Observable.defer {
            Observable.just(likeDao.deleteAll())
        }
    }

    override fun export(): Single<List<Fav>> {
        return Single.defer {
            Single.fromCallable { likeDao.exportData() }
        }
    }

    @WorkerThread
    override fun import(favs: List<Fav>): Int {
        return likeDao.importData(favs.filter { it.liked }).filter { it != -1L }.count()
    }
}