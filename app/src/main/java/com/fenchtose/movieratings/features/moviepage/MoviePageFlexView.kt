package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.widgets.FlexView

class MoviePageFlexView(private val context: Context, private val flexView: FlexView) {

    private val inflater = LayoutInflater.from(context)

    fun setCollections(collections: List<MovieCollection>?) {
        flexView.clearAll()

        collections?.forEach {
            val view = inflater.inflate(R.layout.movie_page_collection_item_layout, flexView, false) as TextView
            view.text = it.name
            flexView.addElement(view)
        }

        flexView.requestLayout()
    }
}