package com.fenchtose.movieratings.model.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class GlideLoader(private val manager: RequestManager): ImageLoader {

    override fun loadDrawable(image: Int, view: ImageView) {
        manager.load(image).into(view)
    }

    override fun loadImage(image: String, view: ImageView, callback: ImageLoader.Callback?) {

        image.takeIf { it.isNotEmpty() && it != "N/A" }?.let {
            manager.load(it).listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    callback?.imageLoaded(it, view)
                    return false
                }

            }).into(view)
        }
    }

    override fun loadImage(image: String, view: ImageView, callback: ImageLoader.SelfLoaderCallback) {
        if (image.isEmpty() || image == "N/A") {
            callback.error(image, view)
            return
        }

        manager.load(image).listener(object : RequestListener<Drawable> {
            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                if (resource == null) {
                    callback.error(image, view)
                } else {
                    callback.imageLoaded(image, view, resource)
                }
                return true
            }

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                callback.error(image, view)
                return true
            }

        }).submit()
    }

    override fun loadImage(image: String, view: ImageView) {
        image.takeIf { it.isNotEmpty() && it != "N/A" }?.let {
            manager.load(it).into(view)
        }
    }

    override fun cancelRequest(view: ImageView) {
        manager.clear(view)
    }
}