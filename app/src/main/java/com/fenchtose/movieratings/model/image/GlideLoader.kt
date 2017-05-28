package com.fenchtose.movieratings.model.image

import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.fenchtose.movieratings.features.search_page.SearchItemViewHolder
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.GlidePalette

class GlideLoader(private val manager: RequestManager): ImageLoader {
    override fun loadImage(image: String, holder: SearchItemViewHolder) {
        manager.load(image)
                .listener(
                        GlidePalette.with(image)
                                .use(BitmapPalette.Profile.VIBRANT).intoBackground(holder.titleView)
//                                .use(BitmapPalette.Profile.VIBRANT).intoTextColor(holder.titleView)
                        )
                .into(holder.imageView)
    }

    override fun loadImage(image: String, view: ImageView) {
        manager.load(image).into(view)
    }

    override fun cancelRequest(view: ImageView) {
        manager.clear(view)
    }
}