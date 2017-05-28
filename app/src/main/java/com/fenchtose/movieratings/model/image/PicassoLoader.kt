package com.fenchtose.movieratings.model.image

import android.content.Context
import android.widget.ImageView
import com.fenchtose.movieratings.features.search_page.SearchItemViewHolder
import com.squareup.picasso.Picasso

class PicassoLoader(context: Context): ImageLoader {

    val loader: Picasso = Picasso.with(context)

    override fun loadImage(image: String, view: ImageView) {
        loader.load(image)
                .into(view)
    }

    override fun cancelRequest(view: ImageView) {
        loader.cancelRequest(view)
    }

    override fun loadImage(image: String, holder: SearchItemViewHolder) {
        loader.load(image)
                .into(holder.imageView)
    }
}