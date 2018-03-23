package com.fenchtose.movieratings.features.baselistpage

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.features.searchpage.SearchPageAdapter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.GlideLoader

abstract class BaseMovieListPageFragment<V: BaseMovieListPage, P: BaseMovieListPresenter<V>>: BaseFragment(), BaseMovieListPage {

    protected var presenter: P? = null

    protected var recyclerView: RecyclerView? = null
    protected var adapter: SearchPageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreated()
        presenter = createPresenter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.base_movies_list_page_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)

        val adapter = SearchPageAdapter(context, GlideLoader(Glide.with(this)),
                object : SearchPageAdapter.AdapterCallback {
                    override fun onLiked(movie: Movie) {
                        presenter?.toggleLike(movie)
                    }

                    override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                        // TODO check for api compatibility
                        presenter?.openMovie(movie, sharedElement)
                    }
                }, createExtraLayoutHelper())

        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter

        presenter?.attachView(this as V)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this as V)
    }

    override fun setData(movies: ArrayList<Movie>) {
        recyclerView?.visibility = View.VISIBLE
        adapter?.data = movies
        adapter?.notifyDataSetChanged()
    }

    open fun onCreated() {

    }

    abstract fun createPresenter(): P

    open fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null
}