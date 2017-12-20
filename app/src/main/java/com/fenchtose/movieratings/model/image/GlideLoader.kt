package com.fenchtose.movieratings.model.image

import android.widget.ImageView
import com.bumptech.glide.RequestManager

class GlideLoader(private val manager: RequestManager): ImageLoader {

    override fun loadImage(image: String, view: ImageView) {
        manager.load(image).into(view)
    }

    override fun cancelRequest(view: ImageView) {
        manager.clear(view)
    }
}