package com.fenchtose.movieratings.base

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.model.entity.Movie

class BaseMovieAdapter(private val context: Context, private val config: AdapterConfig): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    var data: List<Movie> = ArrayList()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount() = config.getItemCount(data)

    override fun getItemId(position: Int) = config.getItemId(data, position)

    override fun getItemViewType(position: Int) = config.getItemViewType(data, position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = config.createViewHolder(inflater, parent, viewType) ?: throw RuntimeException("Invalid view type $viewType")

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        config.bindViewHolder(context, data, holder, position)
    }

    interface AdapterConfig {
        fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder?
        fun bindViewHolder(context: Context, data: List<Movie>, viewHolder: RecyclerView.ViewHolder, position: Int)
        fun getItemCount(data: List<Movie>): Int
        fun getItemId(data: List<Movie>, position: Int): Long
        fun getItemViewType(data: List<Movie>, position: Int): Int
    }

    interface AdapterCallback {
        fun onLiked(movie: Movie)
        fun onClicked(movie: Movie, sharedElement: Pair<View, String>?)
    }

}