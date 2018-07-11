package com.fenchtose.movieratings.model.image

import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.widget.ImageView

interface ImageLoader {

    fun loadDrawable(@DrawableRes image: Int, view: ImageView)
    fun loadImage(image: String, view: ImageView)
    fun loadImage(image: String, view: ImageView, callback: Callback?)
    fun loadImage(image: String, view: ImageView, callback: SelfLoaderCallback)

    fun cancelRequest(view: ImageView)

    interface SelfLoaderCallback {
        fun imageLoaded(image: String, view: ImageView, resource: Drawable)
        fun error(image: String, view: ImageView)
    }

    interface Callback {
        fun imageLoaded(image: String, view: ImageView)
    }
}