package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageState
import com.fenchtose.movieratings.features.baselistpage.Progress
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.features.searchpage.SearchPageFragment
import com.fenchtose.movieratings.model.db.movieCollection.RemoveFromCollection
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.Sort
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.offline.export.ExportCollection
import com.fenchtose.movieratings.util.show

class CollectionPageFragment: BaseMovieListPageFragment() {

    private var emptyStateCta: View? = null

    private var collection: MovieCollection? = null

    private var isEmpty: Boolean = true

    override fun canGoBack() = true

    override fun screenName() = GaScreens.COLLECTION

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun getEmptyContent() = R.string.movie_collection_page_empty_content

    override fun getErrorContent() = R.string.movie_collection_page_error_content

    override fun onCreated() {
        setHasOptionsMenu(true)

        path?.takeIf { it is CollectionPagePath }
                ?.let { (it as CollectionPagePath).collection }
                ?.let { path?.getRouter()?.updateTitle(it.name) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movies_collection_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyStateCta = view.findViewById(R.id.empty_cta)
        emptyStateCta?.setOnClickListener {
            openSearch()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            R.id.action_sort_alphabetically -> {
                GaEvents.SORT.withCategory(path?.category()).withLabelArg(Sort.ALPHABETICAL.name.toLowerCase()).track()
                collection?.let {
                    dispatch?.invoke(CollectionSort(it.id, Sort.ALPHABETICAL))
                }
            }
            R.id.action_sort_year -> {
                GaEvents.SORT.withCategory(path?.category()).withLabelArg(Sort.YEAR.name.toLowerCase()).track()
                collection?.let {
                    dispatch?.invoke(CollectionSort(it.id, Sort.YEAR))
                }
            }
            R.id.action_add_to_collection -> {
                openSearch()
            }
            R.id.action_share -> {
                GaEvents.TAP_SHARE_COLLECTION.track()
                showShareDialog()
            }
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun reduceState(appState: AppState): BaseMovieListPageState {
        if (appState.collectionPages.isEmpty()) {
            return BaseMovieListPageState()
        }

        return BaseMovieListPageState(
                movies = appState.collectionPages.last().movies,
                progress = appState.collectionPages.last().progress
        )
    }

    override fun render(appState: AppState, dispatch: Dispatch) {
        if (appState.collectionPages.isEmpty()) {
            return
        }

        val state = appState.collectionPages.last()
        collection = state.collection
        isEmpty = state.movies.isEmpty()
        when(state.progress) {
            is Progress.Empty -> emptyStateCta?.show()
            else -> emptyStateCta?.show(false)
        }

        state.shareError?.let {
            if (it) {
                showSnackbar(R.string.movie_collection_share_error)
                dispatch(ClearShareError)
            }
        }
    }

    override fun loadingAction() = LoadCollection

    override fun createAdapterConfig(): BaseMovieAdapter.AdapterConfig {
        val glide = GlideLoader(Glide.with(this))
        return CollectionPageAdapterConfig(glide,
                ::toggleLike,
                ::openMovie,
                ::openSearch,
                ::createExtraLayoutHelperMethod)
    }

    override fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? {
        return ::createExtraLayoutHelperMethod
    }

    private fun createExtraLayoutHelperMethod(): SearchItemViewHolder.ExtraLayoutHelper {
        return CollectionRemoveMovieLayoutHelper({
                GaEvents.TAP_REMOVE_MOVIE.track()
                removeMovie(it)
            })
    }

    private fun openSearch() {
        collection?.run {
            path?.getRouter()?.let {
                dispatch?.invoke(Navigation(it, SearchPageFragment.SearchPath.AddToCollection(this)))
            }
        }
    }

    private fun removeMovie(movie: Movie) {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.movie_collection_remove_movie_dialog_title)
                .setMessage(requireContext().getString(R.string.movie_collection_remove_movie_dialog_content, movie.title))
                .setNegativeButton(R.string.movie_collection_remove_movie_negative) { _, _ ->
                    GaEvents.REMOVE_MOVIE.track()
                    collection?.run {
                        dispatch?.invoke(RemoveFromCollection(this, movie))
                    }
                }
                .setNeutralButton(R.string.movie_collection_remove_movie_neutral) { dialog, _ -> dialog.dismiss() }
                .show()

    }

    private fun showShareDialog() {
        if (isEmpty) {
            showSnackbar(R.string.movie_collection_share_empty)
            return
        }

        collection?.let {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.movie_collection_share_dialog_title)
                    .setMessage(R.string.movie_collection_share_dialog_content)
                    .setPositiveButton(R.string.movie_collection_share_dialog_positive_cta) { dialog, _ ->
                        dialog.dismiss()
                        GaEvents.SHARE_COLLECTION.track()
                        dispatch?.invoke(ExportCollection(COLLECTION_PAGE, "collection_${it.name}.txt", it.id))
                    }
                    .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                    .show()
        }

    }
}

class CollectionPagePath(val collection: MovieCollection) : RouterPath<CollectionPageFragment>() {
    override fun createFragmentInstance() = CollectionPageFragment()
    override fun category() = GaCategory.COLLECTION
    override fun showMenuIcons() = intArrayOf(R.id.action_sort, R.id.action_add_to_collection, R.id.action_share)
    override fun initAction() = InitCollectionPage(collection)
    override fun clearAction() = ClearCollectionPage
}