package com.fenchtose.movieratings.features.searchpage

import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.ImageLoader
import com.fenchtose.movieratings.widgets.RatioImageView

class SearchItemViewHolder(
        itemView: View,
        toggleLike: (Movie) -> Unit,
        openMovie: (Movie, Pair<View, String>?) -> Unit,
        extraLayoutCreator: (() -> ExtraLayoutHelper)? = null) : RecyclerView.ViewHolder(itemView) {

    private val imageView: RatioImageView = itemView.findViewById(R.id.imageview)
    private val titleView: TextView = itemView.findViewById(R.id.titleview)
    private val favButton: ImageView = itemView.findViewById(R.id.fav_button)

    private val extraLayoutHelper: ExtraLayoutHelper?

    var movie: Movie? = null

    init {
        favButton.setOnClickListener {
            movie?.let {
                setLiked(!it.liked, false)
                toggleLike(it)
            }
        }

        itemView.setOnClickListener {
            movie?.let {
                val name = "poster$adapterPosition"
                ViewCompat.setTransitionName(imageView, name)
                openMovie(it, Pair(imageView, name))
            }
        }

        extraLayoutHelper = extraLayoutCreator?.invoke()
        extraLayoutHelper?.setup(itemView)
    }

    fun bindMovie(movie: Movie, imageLoader: ImageLoader) {
        titleView.text = "${movie.title}\n(${movie.year})"
        if (movie.poster.contains("http")) {
            imageLoader.loadImage(movie.poster, imageView)
        } else {
            imageLoader.cancelRequest(imageView)
            imageView.setImageBitmap(null)
        }
        setLiked(movie.liked, false)
        this.movie = movie
        extraLayoutHelper?.bind(movie, imageLoader)
    }

    private fun setLiked(liked: Boolean, animate: Boolean) {
        if (!animate || !liked) {
            favButton.setImageResource(if (liked) R.drawable.ic_favorite_yellow_24dp else R.drawable.ic_favorite_border_yellow_24dp)
            return
        }
    }

    interface ExtraLayoutHelper {
        fun setup(itemView: View)
        fun bind(movie: Movie, imageLoader: ImageLoader)
    }
}