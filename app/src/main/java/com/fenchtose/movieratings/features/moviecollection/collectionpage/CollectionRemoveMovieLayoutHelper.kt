package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.features.searchpage.SearchItemViewHolder
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.ImageLoader

class CollectionRemoveMovieLayoutHelper(private val remove: (Movie) -> Unit) : SearchItemViewHolder.ExtraLayoutHelper {

    private var movie: Movie? = null

    override fun setup(itemView: View) {
        val container = itemView.findViewById<FrameLayout?>(R.id.extra_layout_container)
        container?.let {
            it.removeAllViews()
            val layout = LayoutInflater.from(itemView.context).inflate(R.layout.movie_collection_remove_movie_item_layout, it, true)
            val removeCta = layout.findViewById<View?>(R.id.remove_movie_cta)
            removeCta?.setOnClickListener { movie?.let { remove(it) } }
        }
    }

    override fun bind(movie: Movie, imageLoader: ImageLoader) {
        this.movie = movie
    }
}