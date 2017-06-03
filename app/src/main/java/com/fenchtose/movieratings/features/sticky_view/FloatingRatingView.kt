package com.fenchtose.movieratings.features.sticky_view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.fenchtose.movieratings.R

class FloatingRatingView(context: Context) : FrameLayout(context) {

    var ratingView: TextView? = null
    val TAG = "FloatingRatingView"

    fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.floating_rating_view, this, true)
        ratingView = findViewById(R.id.rating_View) as TextView
        setBackgroundResource(R.drawable.floating_rating_view_background)
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