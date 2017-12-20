package com.fenchtose.movieratings.model.image

import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.fenchtose.movieratings.features.search_page.SearchItemViewHolder

class GlideLoader(private val manager: RequestManager): ImageLoader {
    override fun loadImage(image: String, holder: SearchItemViewHolder) {
        manager.load(image)
                .into(holder.imageView)
    }

    override fun loadImage(image: String, view: ImageView) {
        manager.load(image).into(view)
    }

    override fun cancelRequest(view: ImageView) {
        manager.clear(view)
    }
}