package com.fenchtose.movieratings.features.trending

import androidx.annotation.StringRes
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.reduceChild
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageAction
import com.fenchtose.movieratings.features.baselistpage.Progress
import com.fenchtose.movieratings.model.api.provider.MovieRatingsProvider
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.convert
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks

data class TrendingPageState(
    val movies: List<Movie> = listOf(),
    val progress: Progress = Progress.Default,
    val currentTab: TrendingTab = TrendingTab.DAY)

enum class TrendingTab(val key: String, @StringRes val title: Int) {
    DAY("day", R.string.trending_tab_today), WEEK("week", R.string.trending_tab_week)
}

fun AppState.reduceTrendingPage(action: Action): AppState {
    return reduceChild(trendingPage, action, {reduce(it)}, {copy(trendingPage = it)})
}

private fun TrendingPageState.reduce(action: Action): TrendingPageState {
    return when(action) {
        is ClearTrendingPage -> TrendingPageState()
        is BaseMovieListPageAction -> {
            if (action.page == TRENDING_PAGE) {
                when (action) {
                    is BaseMovieListPageAction.Loading -> copy(progress = Progress.Loading)
                    is BaseMovieListPageAction.Loaded -> copy(progress = Progress.Success, movies = action.movies)
                    is BaseMovieListPageAction.Error -> copy(progress = Progress.Error)
                }
            } else {
                this
            }
        }
        is SwitchTab -> if (currentTab == action.tab) this else copy(currentTab = action.tab)
        else -> this
    }

}

object ClearTrendingPage: Action
object LoadTrendingPage: Action
data class SwitchTab(val tab: TrendingTab): Action

const val TRENDING_PAGE = "tp"

class TrendingMoviesMiddleware(private val provider: MovieRatingsProvider,
                               private val rxHooks: RxHooks,
                               private val movieDao: MovieDao,
                               private val likeStore: LikeStore) {

    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action === LoadTrendingPage) {
            if (state.trendingPage.movies.isEmpty()) {
                load(state.trendingPage.currentTab, dispatch)
                return BaseMovieListPageAction.Loading(TRENDING_PAGE)
            }
        } else if (action is SwitchTab) {
            if (state.trendingPage.currentTab != action.tab) {
                AppEvents.selectTrendingTab(action.tab.key).track()
                load(action.tab, dispatch)
                dispatch(BaseMovieListPageAction.Loading(TRENDING_PAGE))
            }
        }

        return next(state, action, dispatch)
    }

    private fun load(tab:TrendingTab, dispatch: Dispatch) {
        provider.getTrending(tab.key)
                .map { it.movies.map { it.convert() } }
                .flatMapIterable { movies -> movies }
                .map {
                    val fromDb = movieDao.getMovieWithImdbId(it.imdbId)
                    fromDb?.let { return@map it.convert() }
                    movieDao.insert(it.convert())
                    it
                }
                .map { likeStore.apply(it) }
                .toList().toObservable()
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    dispatch(BaseMovieListPageAction.Loaded(TRENDING_PAGE, it))
                }, {
                    it.printStackTrace()
                    dispatch(BaseMovieListPageAction.Error(TRENDING_PAGE))
                })
    }

    companion object {
        fun newInstance(): TrendingMoviesMiddleware {
            return TrendingMoviesMiddleware(
                    MovieRatingsApplication.ratingProviderModule.ratingProvider,
                    AppRxHooks(),
                    MovieRatingsApplication.database.movieDao(),
                    DbLikeStore.getInstance(MovieRatingsApplication.database.favDao())
            )
        }
    }

}