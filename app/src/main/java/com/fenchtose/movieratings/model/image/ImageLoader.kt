package com.fenchtose.movieratings.model.image

import android.widget.ImageView

interface ImageLoader {
    fun loadImage(image: String, view: ImageView)
}