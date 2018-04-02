package com.fenchtose.movieratings.widgets.pagesection

import android.support.annotation.StringRes
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.text.bold
import androidx.text.scale

class InlineTextSection(private val contentView: TextView, @StringRes private val resId: Int): PageSection<String?> {
    override fun setContent(content: String?) {
        if (content.isNullOrBlank() || content?.trim() == "N/A") {
            contentView.visibility = View.GONE
            return
        }

        contentView.visibility = View.VISIBLE
        contentView.text = buildEntry(resId, content!!)

    }

    private fun buildEntry(@StringRes id: Int, content: String): SpannableStringBuilder {
        return SpannableStringBuilder(contentView.context.getText(id))
                .bold {
                    scale(1.1f, {
                        append(content)
                    })
                }
    }
}