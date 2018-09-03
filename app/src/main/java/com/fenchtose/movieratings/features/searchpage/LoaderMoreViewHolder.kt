package com.fenchtose.movieratings.features.searchpage

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.fenchtose.movieratings.R

class LoaderMoreViewHolder(itemView: View, private val loadMore: () -> Unit): RecyclerView.ViewHolder(itemView) {
    val button = itemView.findViewById<View>(R.id.load_more_button)
    val progress = itemView.findViewById<View>(R.id.load_more_progressbar)

    init {
        button.setOnClickListener { loadMore() }
        val params = itemView.layoutParams
        if (params is StaggeredGridLayoutManager.LayoutParams) {
            params.isFullSpan = true
        }
    }

    fun bind(status: Boolean) {
        button.visibility = if (status) View.GONE else View.VISIBLE
        progress.visibility = if (status) View.VISIBLE else View.GONE
    }
}