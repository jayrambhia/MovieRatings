package com.fenchtose.movieratings.features.stickyview

import android.content.Context
import android.support.annotation.ColorInt
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.widgets.RatingBubble

class FloatingRating(private val context: Context) {

    val bubble: RatingBubble = RatingBubble(context)

    var movie: Movie? = null
    set(value) {
        field = value
        value?.let {
            setRating(it.ratings[0].value)
        }
    }

    fun updateColor(@ColorInt color: Int) {
        bubble.updateColor(color)
    }

    private fun setRating(rating: String) {
        bubble.setText(context.resources.getString(R.string.floating_rating_content, rating))
    }
}