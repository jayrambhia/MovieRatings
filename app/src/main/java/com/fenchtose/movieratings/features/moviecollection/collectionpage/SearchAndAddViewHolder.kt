package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.support.v7.widget.RecyclerView
import android.view.View

class SearchAndAddViewHolder(itemView: View, callback: CollectionPageAdapterConfig.Callback?): RecyclerView.ViewHolder(itemView) {
    init {
        itemView.setOnClickListener {
            callback?.onAddToCollection()
        }
    }
}