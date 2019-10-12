package com.fenchtose.movieratings.model.db.recentlyBrowsed

import android.content.Context
import androidx.annotation.WorkerThread
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.NoAction
import com.fenchtose.movieratings.model.db.entity.RecentlyBrowsed
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import io.reactivex.Observable
import io.reactivex.Single

interface RecentlyBrowsedStore {
    fun update(data: RecentlyBrowsed)
    fun deleteAll(): Observable<Int>
    fun export(): Single<List<RecentlyBrowsed>>
    @WorkerThread
    fun import(history: List<RecentlyBrowsed>): Int
}

data class RegisterMovie(val movie: Movie): Action
data class MovieRegsitered(val movie: Movie): Action

class RecentlyBrowsedMiddleware(private val store: RecentlyBrowsedStore,
                                private val preferences: UserPreferences) {

    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action is RegisterMovie) {
            return registerMovie(action.movie)
        }

        return next(state, action, dispatch)
    }

    private fun registerMovie(movie: Movie): Action {
        if (!preferences.isAppEnabled(UserPreferences.SAVE_HISTORY)) {
            return NoAction
        }

        val recent = RecentlyBrowsed()
        recent.id = movie.imdbId
        recent.timestamp = System.currentTimeMillis()
        store.update(recent)
        return MovieRegsitered(movie)
    }

    companion object {
        fun newInstance(context: Context): RecentlyBrowsedMiddleware {
            return RecentlyBrowsedMiddleware(
                    DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao()),
                    SettingsPreferences(context)
            )
        }
    }
}