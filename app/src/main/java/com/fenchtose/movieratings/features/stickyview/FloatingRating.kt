package com.fenchtose.movieratings.features.stickyview

import android.content.Context
import android.support.annotation.ColorInt
import android.view.View
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.widgets.RatingBubble

class FloatingRating(private val context: Context, val color: Int, var size: BubbleSize) {

    private val bubble: RatingBubble = RatingBubble(context, color, size)
    var rating: MovieRating? = null
    set(value) {
        field = value
        value?.let {
            updateRating(it)
        }
    }

    fun updateColor(@ColorInt color: Int) {
        bubble.updateColor(color)
    }

    fun updateSize(size: BubbleSize) {
        this.size = size
        bubble.updateSize(size)
        rating?.let { updateRating(it) }
    }

    private fun updateRating(rating: MovieRating) {
        if (size == BubbleSize.BIG) {
            val builder = StringBuilder(rating.title)
            rating.displayYear().takeIf { it.isNotBlank() }?.let {
                builder.append("\n$it")
            }
            builder.append("\n")
            builder.append(rating.displayRating())
            bubble.setText(builder)
        } else {
            bubble.setText(context.resources.getString(R.string.floating_rating_content, rating.displayRating()))
        }
    }

    fun getBubbleView(): RatingBubble = bubble

}

enum class BubbleSize {
    SMALL, BIG
}