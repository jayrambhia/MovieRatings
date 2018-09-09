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
    return updateSearchPage(movie)
            .updateRecentlyBrowsedPage(movie)
            .updateTrendingPage(movie)
            .updateMoviePages(movie)
            .updateCollectionPages(movie)
}

private fun AppState.updateSearchPage(movie: Movie): AppState {
    return if (searchPage.movies.hasMovie(movie) != -1) {
        copy(searchPage = searchPage.copy(movies = searchPage.movies.updateMovie(movie)))
    } else {
        this
    }
}

private fun AppState.updateRecentlyBrowsedPage(movie: Movie): AppState {
    return if (recentlyBrowsedPage.movies.hasMovie(movie) != -1) {
        copy(recentlyBrowsedPage = recentlyBrowsedPage.copy(movies = recentlyBrowsedPage.movies.updateMovie(movie)))
    } else {
        this
    }
}

private fun AppState.updateTrendingPage(movie: Movie): AppState {
    return if (trendingPage.movies.hasMovie(movie) != -1) {
        copy(trendingPage = trendingPage.copy(movies = trendingPage.movies.updateMovie(movie)))
    } else {
        this
    }
}

private fun AppState.updateMoviePages(movie: Movie): AppState {
    val updated = moviePages.map {
        moviePage ->
        if (moviePage.movie.imdbId == movie.imdbId) {
            moviePage.copy(movie = moviePage.movie.like(movie.liked))
        } else {
            moviePage
        }
    }

    if (updated != moviePages) {
        return copy(moviePages = updated)
    }

    return this
}

private fun AppState.updateCollectionPages(movie: Movie): AppState {
    val updated = collectionPages.map {
        if (it.movies.hasMovie(movie) != -1) {
            it.copy(movies = it.movies.updateMovie(movie))
        } else {
            it
        }
    }

    if (updated != collectionPages) {
        return copy(collectionPages = updated)
    }

    return this
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