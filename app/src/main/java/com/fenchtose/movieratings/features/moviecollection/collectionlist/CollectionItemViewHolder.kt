package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.MovieCollection

class CollectionItemViewHolder(itemView: View, callback: CollectionListPageAdapter.AdapterCallback, showDelete: Boolean) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.titleview)
    private val deleteButton = itemView.findViewById<View>(R.id.delete_button)
    private var collection: MovieCollection? = null

    init {
        itemView.setOnClickListener {
            collection?.let { callback.onClicked(it) }
        }

        deleteButton.setOnClickListener {
            collection?.let { callback.onDeleteRequested(it) }
        }

        deleteButton.visibility = if (showDelete) View.VISIBLE else View.GONE
    }

    fun bind(collection: MovieCollection) {
        titleView.text = collection.name
        this.collection = collection
    }

}