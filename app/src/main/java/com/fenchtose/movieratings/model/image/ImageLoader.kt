package com.fenchtose.movieratings.model.image

import android.widget.ImageView
import com.fenchtose.movieratings.features.search_page.SearchItemViewHolder

interface ImageLoader {

    fun loadImage(image: String, holder: SearchItemViewHolder)
    fun loadImage(image: String, view: ImageView)
    fun cancelRequest(view: ImageView)
}