package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.app.AlertDialog
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore

class CollectionPageFragment: BaseMovieListPageFragment<CollectionPage, CollectionPagePresenter>(), CollectionPage {

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun createPresenter(): CollectionPagePresenter {
        return CollectionPagePresenter(DbLikeStore(MovieRatingsApplication.database.favDao()),
                DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                DbMovieCollectionStore(MovieRatingsApplication.database.movieCollectionDao()),
                path?.takeIf { it is CollectionPagePath }?.let { (it as CollectionPagePath).collection })
    }

    override fun onCreated() {
        path?.takeIf { it is CollectionPagePath }
                ?.let { (it as CollectionPagePath).collection }
                ?.let { MovieRatingsApplication.router?.updateTitle(it.name) }
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

    override fun updateState(state: CollectionPage.OpState) {
        val resId = when(state.op) {
            CollectionPage.Op.MOVIE_REMOVED -> R.string.movie_collection_remove_movie_success
            CollectionPage.Op.MOVIE_REMOVE_ERROR -> R.string.movie_collection_remove_movie_error
        }

        showSnackbar(context.getString(resId, state.movie.title))
    }

    class CollectionPagePath(val collection: MovieCollection) : RouterPath<CollectionPageFragment>() {

        override fun createFragmentInstance(): CollectionPageFragment {
            return CollectionPageFragment()
        }

    }
}