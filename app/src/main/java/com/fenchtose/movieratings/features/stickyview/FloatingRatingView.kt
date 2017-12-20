package com.fenchtose.movieratings.features.stickyview

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie

class FloatingRatingView(context: Context) : FrameLayout(context) {

    var movie: Movie? = null
    set(value) {
        field = value
        value?.let {
            setRating(it.ratings[0].value)
        }
    }

    private var ratingView: TextView? = null

    fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.floating_rating_view, this, true)
        ratingView = findViewById(R.id.rating_view)
        setBackgroundResource(R.drawable.floating_rating_view_background)
    }

    private fun setRating(rating: String) {
        ratingView?.let {
            ratingView!!.text = resources.getString(R.string.floating_rating_content, rating)
        }
    }

    init {
        init(context)
    }

}