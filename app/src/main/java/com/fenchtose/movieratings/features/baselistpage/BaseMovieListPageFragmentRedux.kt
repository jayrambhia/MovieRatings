package com.fenchtose.movieratings.features.baselistpage

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.db.like.LikeMovie
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.GlideLoader

abstract class BaseMovieListPageFragmentRedux: BaseFragment() {

    protected var recyclerView: RecyclerView? = null
    protected var adapter: BaseMovieAdapter? = null

    private var stateContent: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreated()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayout(), container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)
        stateContent = view.findViewById(R.id.screen_state_content)
        progressBar = view.findViewById(R.id.progressbar)

        val adapter = BaseMovieAdapter(requireContext(), createAdapterConfig())
        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter
        render { appState, dispatch ->
            render(reduceState(appState), dispatch)
            render(appState, dispatch)
        }
    }

    override fun onResume() {
        super.onResume()
        dispatch?.invoke(loadingAction())
    }

    private fun render(state: BaseMovieListPageState, dispatch: Dispatch) {
        progressBar?.visibility = View.GONE
        when(state.progress) {
            is Progress.Loading -> {
                progressBar?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
            }

            is Progress.Error -> showContentState(getEmptyContent())
            is Progress.Empty -> showContentState(getErrorContent())
            is Progress.Success -> setData(state.movies)
        }
    }

    private fun showContentState(resId: Int) {
        recyclerView?.visibility = View.GONE
        stateContent?.visibility = View.VISIBLE
        stateContent?.setText(resId)
    }

    private fun setData(movies: List<Movie>) {
        adapter?.data?.clear()
        adapter?.data?.addAll(movies)
        adapter?.notifyDataSetChanged()
        stateContent?.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
    }

    open fun getLayout(): Int {
        return R.layout.base_movies_list_page_layout
    }

    abstract fun getErrorContent(): Int

    abstract fun getEmptyContent(): Int

    abstract fun reduceState(appState: AppState): BaseMovieListPageState

    abstract fun loadingAction(): Action

    open fun createAdapterConfig(): BaseMovieAdapter.AdapterConfig {

        val callback = object: BaseMovieAdapter.AdapterCallback {
            override fun onLiked(movie: Movie) {
                GaEvents.LIKE_MOVIE.withCategory(path?.category()).track()
                dispatch?.invoke(LikeMovie(movie, !movie.liked))
            }

            override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                GaEvents.OPEN_MOVIE.withCategory(path?.category()).track()
                path?.getRouter()?.let {
                    dispatch?.invoke(Navigation(it, MoviePageFragment.MoviePath(movie, sharedElement)))
                }
            }
        }

        val glide = GlideLoader(Glide.with(this))

        return BaseMovieListAdapterConfig(callback, glide, createExtraLayoutHelper())
    }

    open fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null

    protected open fun onCreated() {

    }

    protected open fun render(appState: AppState, dispatch: Dispatch) {

    }
}