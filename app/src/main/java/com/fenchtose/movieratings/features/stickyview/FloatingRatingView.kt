package com.fenchtose.movieratings.features.stickyview

import android.content.Context
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.widgets.RatingBubble

class FloatingRatingView(context: Context) : RatingBubble(context) {

    var movie: Movie? = null
    set(value) {
        field = value
        value?.let {
            setRating(it.ratings[0].value)
        }
    }

    private fun setRating(rating: String) {
        setText(resources.getString(R.string.floating_rating_content, rating))
    }
}