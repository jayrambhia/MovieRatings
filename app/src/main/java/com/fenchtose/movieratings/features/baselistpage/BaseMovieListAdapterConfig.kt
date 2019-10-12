package com.fenchtose.movieratings.features.baselistpage

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class BaseMovieListAdapterConfig(val toggleLike: (Movie) -> Unit,
                                 val openMovie: (Movie, Pair<View, String>?) -> Unit,
                                 val glide: ImageLoader,
                                 private val extraLayoutCreator: (() -> SearchItemViewHolder.ExtraLayoutHelper)?): BaseMovieAdapter.AdapterConfig {
    override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return SearchItemViewHolder(inflater.inflate(R.layout.search_movie_item_layout, parent, false),
                toggleLike, openMovie, extraLayoutCreator)
    }

    override fun bindViewHolder(context: Context, data: List<Movie>, viewHolder: RecyclerView.ViewHolder, position: Int) {
        when(viewHolder) {
            is SearchItemViewHolder -> viewHolder.bindMovie(data[position], glide)
        }
    }

    override fun getItemCount(data: List<Movie>) = data.size

    override fun getItemId(data: List<Movie>, position: Int) = data[position].imdbId.hashCode().toLong()

    override fun getItemViewType(data: List<Movie>, position: Int) = 1
}