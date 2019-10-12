package com.fenchtose.movieratings.features.likespage

import android.view.MenuItem
import android.view.View
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageState
import com.fenchtose.movieratings.model.entity.Sort
import com.fenchtose.movieratings.model.db.like.LikeMovie
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.preferences.UserPreferences

class LikesPageFragment: BaseMovieListPageFragment() {

    override fun getScreenTitle() = R.string.likes_page_title

    override fun getEmptyContent() = R.string.likes_page_empty_content

    override fun getErrorContent() = R.string.likes_page_error_content

    override fun onCreated() {
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var consumed = true
        when(item.itemId) {
            R.id.action_sort_alphabetically -> {
                AppEvents.sort(Sort.ALPHABETICAL, path?.category()).track()
                dispatch?.invoke(LikeSort(Sort.ALPHABETICAL))
            }
            R.id.action_sort_year -> {
                AppEvents.sort(Sort.YEAR, path?.category()).track()
                dispatch?.invoke(LikeSort(Sort.YEAR))
            }

            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun render(appState: AppState, dispatch: Dispatch) {
        appState.likesPage.unliked?.let {
            if (!it.shown) {
                showMovieRemoved(it.movie, dispatch)
                dispatch(UndoShown)
            }
        }
    }

    private fun showMovieRemoved(movie: Movie, dispatch: Dispatch) {
        showSnackbarWithAction(
                getString(R.string.movie_unliked_snackbar_content, movie.title),
                R.string.undo_action,
                View.OnClickListener {
                    dispatch.invoke(LikeMovie(movie, true))
                }
        )
    }

    override fun reduceState(appState: AppState): BaseMovieListPageState {
        return BaseMovieListPageState(appState.likesPage.movies, appState.likesPage.progress)
    }

    override fun loadingAction() = LoadLikedMovies

    override fun canGoBack() = true

    override fun screenName() = AppScreens.LIKES

    class LikesPath(private val preferences: UserPreferences): RouterPath<LikesPageFragment>() {
        override fun createFragmentInstance() = LikesPageFragment()
        override fun showMenuIcons(): IntArray {
            val icons = arrayListOf(R.id.action_sort)
            if (preferences.isAppEnabled(UserPreferences.SAVE_HISTORY)) {
                icons.add(R.id.action_history)
            }

            return icons.toIntArray()
        }
        override fun category() = GaCategory.LIKES
        override fun clearAction() = ClearLikedPageState
    }
}