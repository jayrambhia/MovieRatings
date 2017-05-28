package com.fenchtose.movieratings.features.search_page

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class SearchItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.imageview) as ImageView
    val titleView: TextView = itemView.findViewById(R.id.titleview) as TextView

    fun bindMovie(movie: Movie, imageLoader: ImageLoader) {
        titleView.text = movie.title
        imageLoader.loadImage(movie.poster, imageView)
    }
}