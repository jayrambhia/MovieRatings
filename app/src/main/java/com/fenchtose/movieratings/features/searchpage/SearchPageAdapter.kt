package com.fenchtose.movieratings.features.searchpage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class SearchPageAdapter(context: Context,
                        private val imageLoader: ImageLoader,
                        private val callback: AdapterCallback?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var data: ArrayList<Movie> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchItemViewHolder(inflater.inflate(R.layout.search_movie_item_layout, parent, false), callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SearchItemViewHolder).bindMovie(data[position], imageLoader)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return data[position].imdbId.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface AdapterCallback {
        fun onLiked(movie: Movie)
        fun onClicked(movie: Movie, sharedElement: Pair<View, String>?)
    }
}