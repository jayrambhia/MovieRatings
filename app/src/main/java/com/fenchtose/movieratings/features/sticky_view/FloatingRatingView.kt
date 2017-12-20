package com.fenchtose.movieratings.features.sticky_view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie

class FloatingRatingView(context: Context) : FrameLayout(context) {

    var _movie: Movie? = null
    var ratingView: TextView? = null
    val TAG = "FloatingRatingView"

    fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.floating_rating_view, this, true)
        ratingView = findViewById(R.id.rating_view)
        setBackgroundResource(R.drawable.floating_rating_view_background)
    }

    fun updateMovie(movie: Movie) {
        this._movie = movie
        setRating(movie.ratings[0].value)
    }

    fun setRating(rating: String) {
        ratingView?.let {
            ratingView!!.text = resources.getString(R.string.floating_rating_content, rating)
        }
    }

    init {
        init(context)
    }

}