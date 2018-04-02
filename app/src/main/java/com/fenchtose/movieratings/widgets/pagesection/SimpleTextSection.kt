package com.fenchtose.movieratings.widgets.pagesection

import android.support.annotation.StringRes
import android.view.View
import android.widget.TextView

class SimpleTextSection(private val contentView: TextView, @StringRes private val idRes: Int = 0): PageSection<String?> {
    override fun setContent(content: String?) {
        if (content.isNullOrBlank() || content?.trim() == "N/A") {
            contentView.visibility = View.GONE
            return
        }

        contentView.visibility = View.VISIBLE
        if (idRes != 0) {
            contentView.text = contentView.context.getString(idRes, content)
        } else {
            contentView.text = content
        }
    }
}