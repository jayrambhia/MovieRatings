package com.fenchtose.movieratings.features.stickyview

import android.content.Context
import androidx.annotation.ColorInt
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
        val text = when {
            rating.is404() -> context.getString(R.string.floating_rating_404_content)
            else -> when(size) {
                BubbleSize.BIG -> {
                    val builder = StringBuilder()
                    builder.append(rating.title)
                    rating.displayYear().takeIf { it.isNotBlank() }?.let {
                        builder.append("\n$it")
                    }
                    builder.append("\n")
                    builder.append(rating.displayRating())
                    builder.toString()
                }
                BubbleSize.SMALL -> context.getString(R.string.floating_rating_content, rating.displayRating())
            }
        }

        bubble.setText(text)
    }

    fun getBubbleView(): RatingBubble = bubble

}

enum class BubbleSize {
    SMALL, BIG
}