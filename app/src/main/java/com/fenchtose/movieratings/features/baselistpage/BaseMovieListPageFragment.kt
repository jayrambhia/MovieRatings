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
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.GlideLoader

abstract class BaseMovieListPageFragment<V: BaseMovieListPage, P: BaseMovieListPresenter<V>>: BaseFragment(), BaseMovieListPage {

    protected var presenter: P? = null

    protected var recyclerView: RecyclerView? = null
    protected var adapter: BaseMovieAdapter? = null

    private var stateContent: TextView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
        presenter?.restoreState(path?.restoreState())
        onCreated()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.base_movies_list_page_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)
        stateContent = view.findViewById(R.id.screen_state_content)
        progressBar = view.findViewById(R.id.progressbar)

        val adapter = BaseMovieAdapter(requireContext(), createAdapterConfig(presenter))
        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter

        presenter?.attachView(this as V)
    }

    override fun saveState(): PresenterState? {
        return presenter?.saveState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this as V)
        recyclerView?.adapter = null
        adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter = null
    }

    override fun updateState(state: BaseMovieListPage.State) {
        progressBar?.visibility = View.GONE
        when(state) {
            is BaseMovieListPage.State.Loading -> {
                progressBar?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
            }
            is BaseMovieListPage.State.Success -> setData(state.movies)
            is BaseMovieListPage.State.Empty -> showContentState(getEmptyContent())
            is BaseMovieListPage.State.Error -> showContentState(getErrorContent())
        }
    }

    private fun showContentState(resId: Int) {
        recyclerView?.visibility = View.GONE
        stateContent?.visibility = View.VISIBLE
        stateContent?.setText(resId)
    }

    private fun setData(movies: List<Movie>) {
        adapter?.data = movies
        adapter?.notifyDataSetChanged()
        stateContent?.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
    }

    abstract fun createPresenter(): P

    abstract fun getErrorContent(): Int

    abstract fun getEmptyContent(): Int

    open fun createAdapterConfig(presenter: P?): BaseMovieAdapter.AdapterConfig {

        val callback = object: BaseMovieAdapter.AdapterCallback {
            override fun onLiked(movie: Movie) {
                presenter?.toggleLike(movie)
            }

            override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                presenter?.openMovie(movie, sharedElement)
            }
        }

        val glide = GlideLoader(Glide.with(this))

        return BaseMovieListAdapterConfig(callback, glide, createExtraLayoutHelper())
    }

    open fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null

    open protected fun onCreated() {

    }
}