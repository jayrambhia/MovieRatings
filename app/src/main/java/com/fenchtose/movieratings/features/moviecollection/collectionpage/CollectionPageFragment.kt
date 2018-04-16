package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.app.AlertDialog
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.features.searchpage.SearchPageAdapter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.preferences.SettingsPreferences

class CollectionPageFragment: BaseMovieListPageFragment<CollectionPage, CollectionPagePresenter>(), CollectionPage {

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun getEmptyContent() = R.string.movie_collection_page_empty_content

    override fun getErrorContent() = R.string.movie_collection_page_error_content

    override fun createPresenter(): CollectionPagePresenter {
        return CollectionPagePresenter(DbLikeStore(MovieRatingsApplication.database.favDao()),
                DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                DbMovieCollectionStore(MovieRatingsApplication.database.movieCollectionDao()),
                SettingsPreferences(context),
                path?.takeIf { it is CollectionPagePath }?.let { (it as CollectionPagePath).collection })
    }

    override fun onCreated() {
        setHasOptionsMenu(true)

        path?.takeIf { it is CollectionPagePath }
                ?.let { (it as CollectionPagePath).collection }
                ?.let { MovieRatingsApplication.router?.updateTitle(it.name) }
    }

    override fun createAdapterConfig(presenter: CollectionPagePresenter?): BaseMovieAdapter.AdapterConfig {
        val glide = GlideLoader(Glide.with(this))

        val callback = object: SearchPageAdapter.AdapterCallback {
            override fun onLiked(movie: Movie) {
                presenter?.toggleLike(movie)
            }

            override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                presenter?.openMovie(movie, sharedElement)
            }
        }

        return CollectionPageAdapterConfig(callback, glide, ::createExtraLayoutHelperMethod)
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

    override fun updateState(state: CollectionPage.OpState) {
        val resId = when(state) {
            is CollectionPage.OpState.Removed -> {
                adapter?.notifyItemRemoved(state.position)
                R.string.movie_collection_remove_movie_success
            }

            is CollectionPage.OpState.RemoveError -> R.string.movie_collection_remove_movie_error
            is CollectionPage.OpState.Added-> {
                adapter?.notifyItemInserted(state.position)
                R.string.movie_collection_add_movie_success
            }
            is CollectionPage.OpState.AddError -> R.string.movie_collection_add_movie_error
        }

        if (state is CollectionPage.OpState.Removed) {
            showSnackbarWithAction(context.getString(resId, state.movie.title), R.string.undo_action,
                    View.OnClickListener { presenter?.undoRemove(state.movie, state.position) })
        } else {
            showSnackbar(context.getString(resId, state.movie.title))
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