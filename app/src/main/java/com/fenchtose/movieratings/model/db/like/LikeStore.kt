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
import com.fenchtose.movieratings.model.entity.hasMovie
import com.fenchtose.movieratings.model.entity.updateMovie
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

fun AppState.reduceLiked(action: Action): AppState {
    if (action !is MovieLiked) {
        return this
    }

    val movie = action.movie

    // Don't make unnecessary copies
    var updated = this
    if (searchPage.movies.hasMovie(movie) != -1) {
        updated = updated.copy(searchPage = searchPage.copy(movies = searchPage.movies.updateMovie(movie)))
    }
    if (recentlyBrowsedPage.movies.hasMovie(movie) != -1) {
        updated = updated.copy(recentlyBrowsedPage = recentlyBrowsedPage.copy(movies = recentlyBrowsedPage.movies.updateMovie(movie)))
    }
    if (trendingPage.movies.hasMovie(movie) != -1) {
        updated = updated.copy(trendingPage = trendingPage.copy(movies = trendingPage.movies.updateMovie(movie)))
    }
    if (moviePage.movie.imdbId == action.movie.imdbId) {
        updated = updated.copy(moviePage = moviePage.copy(movie = moviePage.movie.like(action.movie.liked)))
    }

    return updated
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