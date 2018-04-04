package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.app.AlertDialog
import android.view.MenuItem
import android.view.View
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.di.DependencyProvider
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.preferences.SettingsPreferences

class CollectionPageFragment: BaseMovieListPageFragment<CollectionPage, CollectionPagePresenter>(), CollectionPage {

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun getEmptyContent() = R.string.movie_collection_page_empty_content

    override fun getErrorContent() = R.string.movie_collection_page_error_content

    override fun createPresenter(): CollectionPagePresenter? {
        DependencyProvider.di()?.let {
            it.database?.run {
                return CollectionPagePresenter(DbLikeStore(favDao()),
                        DbMovieCollectionProvider(movieCollectionDao()),
                        DbMovieCollectionStore(movieCollectionDao()),
                        SettingsPreferences(context),
                        path?.takeIf { it is CollectionPagePath }?.let { (it as CollectionPagePath).collection },
                        it.router)
            }

        }

        return null
    }

    override fun onCreated() {
        setHasOptionsMenu(true)

        path?.takeIf { it is CollectionPagePath }
                ?.let { (it as CollectionPagePath).collection }
                ?.let { DependencyProvider.di()?.router?.updateTitle(it.name) }
    }

    override fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? {
        return ::createExtraLayoutHelperMethod
    }

    private fun createExtraLayoutHelperMethod(): SearchItemViewHolder.ExtraLayoutHelper {
        return CollectionRemoveMovieLayoutHelper(object : CollectionRemoveMovieLayoutHelper.Callback {
            override fun onRemoveRequested(movie: Movie) {
                removeMovie(movie)
            }
        })
    }

    private fun removeMovie(movie: Movie) {
        AlertDialog.Builder(context)
                .setTitle(R.string.movie_collection_remove_movie_dialog_title)
                .setMessage(context.getString(R.string.movie_collection_remove_movie_dialog_content, movie.title))
                .setNegativeButton(R.string.movie_collection_remove_movie_negative) { _, _ -> presenter?.removeMovie(movie) }
                .setNeutralButton(R.string.movie_collection_remove_movie_neutral) { dialog, _ -> dialog.dismiss() }
                .show()

    }

    override fun onRemoved(movie: Movie, position: Int) {
        adapter?.notifyItemRemoved(position)

    }

    override fun showAdded(movie: Movie, position: Int) {
        adapter?.notifyItemInserted(position)
    }

    override fun updateState(state: CollectionPage.OpState) {
        val resId = when(state.op) {
            CollectionPage.Op.MOVIE_REMOVED -> R.string.movie_collection_remove_movie_success
            CollectionPage.Op.MOVIE_REMOVE_ERROR -> R.string.movie_collection_remove_movie_error
            CollectionPage.Op.MOVIE_ADDED -> R.string.movie_collection_add_movie_success
            CollectionPage.Op.MOVIE_ADD_ERROR -> R.string.movie_collection_add_movie_error
        }

        if (state.op != CollectionPage.Op.MOVIE_REMOVED) {
            showSnackbar(context.getString(resId, state.movie.title))
        } else {
            showSnackbarWithAction(context.getString(resId, state.movie.title), R.string.undo_action,
                    View.OnClickListener { presenter?.undoRemove(state.movie, state.position) })
        }
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

    class CollectionPagePath(val collection: MovieCollection) : RouterPath<CollectionPageFragment>() {

        override fun createFragmentInstance(): CollectionPageFragment {
            return CollectionPageFragment()
        }

        override fun showMenuIcons(): IntArray {
            return intArrayOf(R.id.action_sort)
        }
    }
}