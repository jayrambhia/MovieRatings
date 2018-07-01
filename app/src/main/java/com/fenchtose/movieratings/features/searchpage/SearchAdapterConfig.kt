package com.fenchtose.movieratings.features.searchpage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class SearchAdapterConfig(
        private val imageLoader: ImageLoader,
        private val callback: SearchCallback?,
        private val extraLayoutCreator: (() -> SearchItemViewHolder.ExtraLayoutHelper)? = null
    ): BaseMovieAdapter.AdapterConfig {

    private val TYPE_LOADER = 2
    private val TYPE_MOVIE = 1

    private var showLoadingMore = false

    var adapter: BaseMovieAdapter? = null

    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when(viewType) {
            TYPE_MOVIE -> SearchItemViewHolder(inflater.inflate(R.layout.search_movie_item_layout, parent, false), callback, extraLayoutCreator)
            TYPE_LOADER -> LoaderMoreViewHolder(inflater.inflate(R.layout.load_more_item_view_layout, parent, false), callback)
            else -> null
        }
    }

    override fun bindViewHolder(context: Context, data: List<Movie>, viewHolder: RecyclerView.ViewHolder, position: Int) {
        when(viewHolder) {
            is LoaderMoreViewHolder -> viewHolder.bind(showLoadingMore)
            is SearchItemViewHolder -> viewHolder.bindMovie(data[position], imageLoader)
        }
    }

    override fun getItemCount(data: List<Movie>): Int {
        return if (data.isNotEmpty()) data.size + 1 else data.size
    }

    override fun getItemId(data: List<Movie>, position: Int): Long {
        return if (position < data.size) data[position].imdbId.hashCode().toLong() else "loading".hashCode().toLong()
    }

    override fun getItemViewType(data: List<Movie>, position: Int): Int {
        return if (position != 0 && position == getItemCount(data) - 1) TYPE_LOADER else TYPE_MOVIE
    }

    fun showLoadingMore(status: Boolean) {
        showLoadingMore = status
        adapter?.let {
            val count = it.itemCount
            if (count != 0) {
                it.notifyItemChanged(count - 1)
            }
        }

    }

    interface SearchCallback: BaseMovieAdapter.AdapterCallback, LoadMoreCallback

    interface LoadMoreCallback {
        fun onLoadMore()
    }

}