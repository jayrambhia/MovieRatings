package com.fenchtose.movieratings.features.likespage

import android.view.MenuItem
import android.view.View
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.DbFavoriteMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.util.AppRxHooks

class LikesPageFragment: BaseMovieListPageFragment<LikesPage, LikesPresenter>(), LikesPage {

    override fun getScreenTitle() = R.string.likes_page_title

    override fun getEmptyContent() = R.string.likes_page_empty_content

    override fun getErrorContent() = R.string.likes_page_error_content

    override fun onCreated() {
        setHasOptionsMenu(true)
    }

    override fun createPresenter(): LikesPresenter {
        val dao = MovieRatingsApplication.database.movieDao()
        val favoriteProvider = DbFavoriteMovieProvider(dao)
        val likeStore = DbLikeStore.getInstance(MovieRatingsApplication.database.favDao())
        val userPreferences = SettingsPreferences(context)
        return LikesPresenter(AppRxHooks(), favoriteProvider, likeStore, userPreferences)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            R.id.action_sort_alphabetically -> presenter?.sort(Sort.ALPHABETICAL)
//            R.id.action_sort_genre -> presenter?.sort(Sort.GENRE)
            R.id.action_sort_year -> presenter?.sort(Sort.YEAR)
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun showRemoved(movie: Movie, index: Int) {
        adapter?.notifyItemRemoved(index)
        showMovieRemoved(movie, index)
    }

    override fun showAdded(movie: Movie, index: Int) {
        adapter?.notifyItemInserted(index)
        recyclerView?.post {
            recyclerView?.scrollToPosition(index)
        }
    }

    private fun showMovieRemoved(movie: Movie, index: Int) {
        showSnackbarWithAction(
                getString(R.string.movie_unliked_snackbar_content, movie.title),
                R.string.undo_action,
                View.OnClickListener {
                    presenter?.undoUnlike(movie, index)
                }
        )
    }

    override fun canGoBack() = true

    class LikesPath : RouterPath<LikesPageFragment>() {
        override fun createFragmentInstance(): LikesPageFragment {
            return LikesPageFragment()
        }

        override fun showMenuIcons(): IntArray {
            return intArrayOf(R.id.action_sort)
        }
    }
}