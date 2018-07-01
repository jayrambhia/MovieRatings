package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class CollectionPageAdapterConfig(val callback: Callback, val glide: ImageLoader,
                                  private val extraLayoutCreator: (() -> SearchItemViewHolder.ExtraLayoutHelper)): BaseMovieAdapter.AdapterConfig {
    val MOVIE = 1
    val ADD = 2

    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when(viewType) {
            MOVIE ->  SearchItemViewHolder(inflater.inflate(R.layout.search_movie_item_layout, parent, false), callback, extraLayoutCreator)
            ADD -> SearchAndAddViewHolder(inflater.inflate(R.layout.add_movie_item_layout, parent, false), callback)
            else -> null
        }
    }

    override fun bindViewHolder(context: Context, data: List<Movie>, viewHolder: RecyclerView.ViewHolder, position: Int) {
        when(viewHolder) {
            is SearchItemViewHolder -> viewHolder.bindMovie(data[position], glide)
        }
    }

    override fun getItemCount(data: List<Movie>): Int {
        return if (data.isEmpty()) 0 else data.size + 1
    }

    override fun getItemId(data: List<Movie>, position: Int): Long {
        return if (data.isEmpty()) RecyclerView.NO_ID else (if (position >= data.size) "add-search".hashCode().toLong() else data[position].imdbId.hashCode().toLong())
    }

    override fun getItemViewType(data: List<Movie>, position: Int): Int {
        return if (data.isEmpty()) -1 else (if (position >= data.size) ADD else MOVIE)
    }

    interface Callback: BaseMovieAdapter.AdapterCallback {
        fun onAddToCollection()
    }
}