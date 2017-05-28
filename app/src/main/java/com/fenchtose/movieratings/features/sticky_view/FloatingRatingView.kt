package com.fenchtose.movieratings.features.sticky_view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.fenchtose.movieratings.R

class FloatingRatingView : FrameLayout {

    var ratingView: TextView? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.floating_rating_view, this, true)
        ratingView = findViewById(R.id.rating_View) as TextView
    }

    fun setRating(rating: String) {
        ratingView?.let {
            ratingView!!.text = rating
        }
    }
}