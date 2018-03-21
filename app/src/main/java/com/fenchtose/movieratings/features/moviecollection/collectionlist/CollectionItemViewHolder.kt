package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.MovieCollection

class CollectionItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.titleview)

    init {

        itemView.setOnClickListener {

        }
    }

    fun bind(collection: MovieCollection) {
        titleView.text = collection.name
    }

}