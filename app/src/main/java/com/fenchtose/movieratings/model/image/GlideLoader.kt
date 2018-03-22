package com.fenchtose.movieratings.model.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class GlideLoader(private val manager: RequestManager): ImageLoader {
    override fun loadImage(image: String, view: ImageView, callback: ImageLoader.Callback?) {

        manager.load(image).listener(object: RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                callback?.imageLoaded(image, view)
                return false
            }

        }).into(view)


    }

    override fun loadImage(image: String, view: ImageView) {
        manager.load(image).into(view)
    }

    override fun cancelRequest(view: ImageView) {
        manager.clear(view)
    }
}