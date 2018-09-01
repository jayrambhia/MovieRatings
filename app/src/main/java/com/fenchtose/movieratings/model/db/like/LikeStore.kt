package com.fenchtose.movieratings.model.db.like

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.model.db.entity.Fav
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.entity.Movie
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface LikeStore: UserPreferenceApplier {
    @WorkerThread
    fun isLiked(imdbId: String): Boolean
    fun setLiked(imdbId: String, liked: Boolean)
    fun deleteAll(): Observable<Int>
    fun export(): Single<List<Fav>>
    @WorkerThread
    fun import(favs: List<Fav>): Int
}

data class LikeMovie(val movie: Movie, val liked: Boolean): Action
data class MovieLiked(val movie: Movie): Action

class LikeMiddleware(private val likeStore: LikeStore) {

    fun likeMiddleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action is LikeMovie) {
            toggleLike(action, dispatch)
        }
        return next(state, action, dispatch)
    }

    private fun toggleLike(action: LikeMovie, dispatch: Dispatch) {
        Observable.just(action)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    likeStore.setLiked(it.movie.imdbId, it.liked)
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dispatch(MovieLiked(it.movie.copy(liked = it.liked)))
                })
    }

    companion object {
        fun newInstance(): LikeMiddleware {
            return LikeMiddleware(DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()))
        }
    }
}