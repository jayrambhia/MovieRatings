package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.image.ImageLoader
import com.fenchtose.movieratings.widgets.album.AlbumViewHelper
import com.fenchtose.movieratings.widgets.album.BasicAlbumStrategy

class CollectionItemViewHolder(itemView: View, callback: CollectionListPageAdapter.AdapterCallback, showDelete: Boolean) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.titleview)
    private val deleteButton = itemView.findViewById<View>(R.id.delete_button)
    private val deleteContainer = itemView.findViewById<View>(R.id.delete_container)
    private val totalCount: TextView = itemView.findViewById(R.id.total_count)
    private val imageView: ImageView = itemView.findViewById(R.id.imageview)
    private var collection: MovieCollection? = null
    private val albumHelper = AlbumViewHelper(imageView, BasicAlbumStrategy(), 4)

    init {
        itemView.setOnClickListener {
            collection?.let { callback.onClicked(it) }
        }

        deleteButton.setOnClickListener {
            collection?.let { callback.onDeleteRequested(it) }
        }

        deleteContainer.visibility = if (showDelete) View.VISIBLE else View.GONE
    }

    fun bind(collection: MovieCollection, imageLoader: ImageLoader) {
        titleView.text = collection.name
        totalCount.text = collection.movies.size.toString()
        this.collection = collection
        albumHelper.loadImages(collection.movies.map { it.poster }.shuffled(), imageLoader)
    }

}