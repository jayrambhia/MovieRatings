package com.fenchtose.movieratings.widgets.pagesection

import android.view.View
import android.widget.TextView

class TextSection(private val header: View, private val contentView: TextView) : PageSection<String?> {
    override fun setContent(content: String?) {
        if (content.isNullOrBlank() || content?.trim() == "N/A") {
            header.visibility = View.GONE
            contentView.visibility = View.GONE
        } else {
            header.visibility = View.VISIBLE
            contentView.visibility = View.VISIBLE
            contentView.text = content
        }
    }
}