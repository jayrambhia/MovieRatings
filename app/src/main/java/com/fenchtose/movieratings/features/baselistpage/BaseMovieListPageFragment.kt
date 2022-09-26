package com.fenchtose.movieratings.features.baselistpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.moviepage.MoviePath
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.db.like.LikeMovie
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.GlideLoader
import com.google.accompanist.appcompattheme.AppCompatTheme

abstract class BaseMovieListPageFragment : BaseFragment() {

    private lateinit var composeView: ComposeView
    private lateinit var viewModel: MovieListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayout(), container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView = view.findViewById(R.id.compose_root)
    }

    override fun onResume() {
        super.onResume()
        render { appState, dispatch ->
            render(appState, reduceState(appState), dispatch)
            render(appState, dispatch)
        }
        dispatch?.invoke(loadingAction())
    }

    private fun render(appState: AppState, state: BaseMovieListPageState, dispatch: Dispatch) {
        composeView.setContent {
            AppCompatTheme() {
                Column {
                    Header(appState, dispatch)
                    BaseMovieListComponent(
                        progress = state.progress,
                        movies = state.movies,
                        itemFooter = { movie, dispatch -> ItemFooter(movie = movie, dispatch = dispatch) },
                        dispatch = dispatch,
                        errorRes = getErrorContent(),
                        emptyContentRes = getEmptyContent(),
                        likeMovie = ::toggleLike,
                        openMovie = ::openMovie
                    )
                }
            }
        }
    }

    @Composable
    open fun Header(appState: AppState, dispatch: Dispatch) {

    }

    @Composable
    open fun ItemFooter(movie: Movie, dispatch: Dispatch?) {

    }

    open fun getLayout(): Int {
        return R.layout.base_movies_list_page_layout
    }

    abstract fun getErrorContent(): Int

    abstract fun getEmptyContent(): Int

    abstract fun reduceState(appState: AppState): BaseMovieListPageState

    abstract fun loadingAction(): Action

    open fun createAdapterConfig(): BaseMovieAdapter.AdapterConfig {
        val glide = GlideLoader(Glide.with(this))
        return BaseMovieListAdapterConfig(
            ::toggleLike,
            ::openMovie,
            glide,
            createExtraLayoutHelper()
        )
    }

    open fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null

    protected open fun onCreated() {

    }

    protected open fun render(appState: AppState, dispatch: Dispatch) {

    }

    protected open fun toggleLike(movie: Movie) {
        AppEvents.like(path?.category(), !movie.liked).track()
        dispatch?.invoke(LikeMovie(movie, !movie.liked))
    }

    protected open fun openMovie(movie: Movie, sharedElement: Pair<View, String>? = null) {
        AppEvents.openMovie(path?.category() ?: "unknown").track()
        path?.getRouter()?.let {
            dispatch?.invoke(Navigation(it, MoviePath(movie, sharedElement)))
        }
    }
}