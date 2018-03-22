package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.widgets.FlexView

class MoviePageFlexView(private val context: Context, private val flexView: FlexView) {

    private val inflater = LayoutInflater.from(context)

    fun setCollections(collections: List<MovieCollection>?) {
        flexView.clearAll()

        collections?.forEach {
            val view = TextView(context)
            view.textSize = 36f
            view.text = it.name
            flexView.addElement(view)
        }

        flexView.requestLayout()
    }
}