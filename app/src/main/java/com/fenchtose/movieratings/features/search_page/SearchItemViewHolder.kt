package com.fenchtose.movieratings.features.search_page

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.ImageLoader
import com.fenchtose.movieratings.widgets.RatioImageView

class SearchItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: RatioImageView = itemView.findViewById(R.id.imageview) as RatioImageView
    val titleView: TextView = itemView.findViewById(R.id.titleview) as TextView
    val favButton: ImageView = itemView.findViewById(R.id.fav_button) as ImageView

    init {
        favButton.setOnClickListener {

        }
    }

    fun bindMovie(movie: Movie, imageLoader: ImageLoader) {
        titleView.text = movie.title
        if (movie.poster.contains("http")) {
            imageLoader.loadImage(movie.poster, this)
//            titleView.visibility = View.GONE
        } else {
            imageLoader.cancelRequest(imageView)
            imageView.setImageBitmap(null)
//            titleView.visibility = View.VISIBLE
        }
    }
}