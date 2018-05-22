package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.offline.export.DataFileExporter
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.util.AppFileUtils
import com.fenchtose.movieratings.util.IntentUtils

class CollectionPageFragment: BaseMovieListPageFragment<CollectionPage, CollectionPagePresenter>(), CollectionPage {

    private var emptyStateCta: View? = null

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun getEmptyContent() = R.string.movie_collection_page_empty_content

    override fun getErrorContent() = R.string.movie_collection_page_error_content

    override fun createPresenter(): CollectionPagePresenter {
        return CollectionPagePresenter(DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                AppFileUtils(),
                DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                SettingsPreferences(context),
                DataFileExporter.newInstance(MovieRatingsApplication.database),
                path?.takeIf { it is CollectionPagePath }?.let { (it as CollectionPagePath).collection })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movies_collection_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyStateCta = view.findViewById(R.id.empty_cta)
        emptyStateCta?.setOnClickListener {
            presenter?.searchToAddToCollection()
        }
    }

    override fun updateState(state: BaseMovieListPage.State) {
        super.updateState(state)
        val visible = when(state) {
            is BaseMovieListPage.State.Empty -> View.VISIBLE
            else -> View.GONE
        }

        emptyStateCta?.visibility = visible
    }

    override fun onCreated() {
        setHasOptionsMenu(true)

        path?.takeIf { it is CollectionPagePath }
                ?.let { (it as CollectionPagePath).collection }
                ?.let { MovieRatingsApplication.router?.updateTitle(it.name) }
    }

    override fun createAdapterConfig(presenter: CollectionPagePresenter?): BaseMovieAdapter.AdapterConfig {
        val glide = GlideLoader(Glide.with(this))

        val callback = object: CollectionPageAdapterConfig.Callback {
            override fun onLiked(movie: Movie) {
                presenter?.toggleLike(movie)
            }

            override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                presenter?.openMovie(movie, sharedElement)
            }

            override fun onAddToCollection() {
                presenter?.searchToAddToCollection()
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

    private fun showShareDialog() {
        if (presenter?.canShare() == true) {
            android.support.v7.app.AlertDialog.Builder(context)
                    .setTitle(R.string.movie_collection_share_dialog_title)
                    .setMessage(R.string.movie_collection_share_dialog_content)
                    .setPositiveButton(R.string.movie_collection_share_dialog_positive_cta) {
                        dialog, _ ->
                        dialog.dismiss()
                        presenter?.share()
                    }
                    .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                    .show()
            return
        }

        showSnackbar(R.string.movie_collection_share_empty)
    }

    override fun updateState(state: CollectionPage.OpState) {
        val resId = when(state) {
            is CollectionPage.OpState.Removed -> {
                adapter?.let {
                    if (it.itemCount > state.position) {
                        it.notifyItemRemoved(state.position)
                    } else {
                        it.notifyDataSetChanged()
                    }
                }
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

    override fun updateState(state: CollectionPage.ShareState) {
        when(state) {
            is CollectionPage.ShareState.Started -> {}
            is CollectionPage.ShareState.Error -> showSnackbar(R.string.movie_collection_share_error)
            is CollectionPage.ShareState.Success -> IntentUtils.openShareFileIntent(context, state.uri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            R.id.action_sort_alphabetically -> presenter?.sort(Sort.ALPHABETICAL)
//            R.id.action_sort_genre -> presenter?.sort(Sort.GENRE)
            R.id.action_sort_year -> presenter?.sort(Sort.YEAR)
            R.id.action_add_to_collection -> presenter?.searchToAddToCollection()
            R.id.action_share -> showShareDialog()
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    class CollectionPagePath(val collection: MovieCollection) : RouterPath<CollectionPageFragment>() {

        override fun createFragmentInstance(): CollectionPageFragment {
            return CollectionPageFragment()
        }

        override fun showMenuIcons(): IntArray {
            return intArrayOf(R.id.action_sort, R.id.action_add_to_collection, R.id.action_share)
        }
    }
}