package com.fenchtose.movieratings.model.image

import android.widget.ImageView

interface ImageLoader {

    fun loadImage(image: String, view: ImageView)
    fun loadImage(image: String, view: ImageView, callback: Callback?)
    fun cancelRequest(view: ImageView)

    interface Callback {
        fun imageLoaded(image: String, view: ImageView)
    }
}