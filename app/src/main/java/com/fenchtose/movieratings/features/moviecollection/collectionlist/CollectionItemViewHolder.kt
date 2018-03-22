package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.MovieCollection

class CollectionItemViewHolder(itemView: View, callback: CollectionListPageAdapter.AdapterCallback) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.titleview)
    private var collection: MovieCollection? = null

    init {
        itemView.setOnClickListener {
            collection?.let { callback.onClicked(it) }
        }
    }

    fun bind(collection: MovieCollection) {
        titleView.text = collection.name
        this.collection = collection
    }

}