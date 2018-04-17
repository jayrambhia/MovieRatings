package com.fenchtose.movieratings.features.searchpage

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class AddToCollectionMovieLayoutHelper(private val callback: Callback) : SearchItemViewHolder.ExtraLayoutHelper {

    private var movie: Movie? = null

    override fun setup(itemView: View) {
        val container = itemView.findViewById<FrameLayout?>(R.id.extra_layout_container)
        container?.let {
            it.removeAllViews()
            val layout = LayoutInflater.from(itemView.context).inflate(R.layout.movie_collection_add_movie_item_layout, it, true)
            val addCta = layout.findViewById<View?>(R.id.add_movie_cta)
            addCta?.setOnClickListener { movie?.let { callback.onAddRequested(it) } }
        }
    }

    override fun bind(movie: Movie, imageLoader: ImageLoader) {
        this.movie = movie
    }

    interface Callback {
        fun onAddRequested(movie: Movie)
    }
}