package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Unsubscribe
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageFragment
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageState
import com.fenchtose.movieratings.features.searchpage.ClearCollectionOp
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.features.searchpage.SearchPageFragment
import com.fenchtose.movieratings.model.db.movieCollection.AddToCollection
import com.fenchtose.movieratings.model.db.movieCollection.CollectionViewMiddleware
import com.fenchtose.movieratings.model.db.movieCollection.ConfirmRemove
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

    override fun screenName() = AppScreens.COLLECTION

    override fun getScreenTitle() = R.string.movie_collection_page_title

    override fun getEmptyContent() = R.string.movie_collection_page_empty_content

    override fun getErrorContent() = R.string.movie_collection_page_error_content

    private var viewMiddlewareUnsubscribe: Unsubscribe = {}

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
        viewMiddlewareUnsubscribe = MovieRatingsApplication.store.addViewMiddleware(CollectionViewMiddleware.newInstance(requireContext())::middleware)
        emptyStateCta = view.findViewById(R.id.empty_cta)
        emptyStateCta?.setOnClickListener {
            openSearch()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var consumed = true
        when(item.itemId) {
            R.id.action_sort_alphabetically -> {
                AppEvents.sort(Sort.ALPHABETICAL, path?.category()).track()
                collection?.let {
                    dispatch?.invoke(CollectionSort(it.id, Sort.ALPHABETICAL))
                }
            }
            R.id.action_sort_year -> {
                AppEvents.sort(Sort.YEAR, path?.category()).track()
                collection?.let {
                    dispatch?.invoke(CollectionSort(it.id, Sort.YEAR))
                }
            }
            R.id.action_add_to_collection -> {
                openSearch()
            }
            R.id.action_share -> {
                showShareDialog()
            }
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewMiddlewareUnsubscribe()
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
        emptyStateCta?.show(isEmpty)

        state.shareError?.let {
            if (it) {
                showSnackbar(R.string.movie_collection_share_error)
                dispatch(ClearShareError)
            }
        }

        state.collectionOp?.let {
            if (it is MovieCollectionOp.Removed) {
                AppEvents.addToCollection(path?.category()).track()
                showSnackbarWithAction(requireContext().getString(R.string.movie_collection_remove_movie_success, it.movie.title), R.string.undo_action,
                        View.OnClickListener { dispatch.invoke(AddToCollection(state.collectionOp.collection, state.collectionOp.movie)) })

                dispatch(ClearCollectionOp)
                return@let
            }

            val resId = when(it) {
                is MovieCollectionOp.RemoveError -> R.string.movie_collection_remove_movie_error
                is MovieCollectionOp.Added -> R.string.movie_collection_add_movie_success
                is MovieCollectionOp.AddError -> R.string.movie_collection_add_movie_error
                else -> 0
            }

            if (resId != 0) {
                showSnackbar(requireContext().getString(resId, it.movie.title))
            }

            dispatch(ClearCollectionOp)
        }
    }

    @Composable
    override fun ItemFooter(movie: Movie, dispatch: Dispatch?) {
//        val collection = this.collection ?: return
        Text(
            text = stringResource(id = R.string.movie_collection_page_remove_cta),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { dispatch?.invoke(ConfirmRemove(collection!!, movie)) },
            color = colorResource(id = R.color.colorPrimary),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
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
        return CollectionRemoveMovieLayoutHelper { removeMovie(it) }
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
                    AppEvents.REMOVE_MOVIE.track()
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
                        AppEvents.SHARE_COLLECTION.track()
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