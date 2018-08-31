package com.fenchtose.movieratings.widgets.pagesection

import android.view.View
import android.widget.TextView
import com.fenchtose.movieratings.analytics.events.Event

class ExpandableSection(private val header: View,
                        private val toggleButton: View,
                        private val contentView: TextView,
                        private val expandEvent: Event? = null,
                        private val collapseEvent: Event? = null) : PageSection<String?> {
    private var isExpanded = false

    override fun setContent(content: String?) {
        contentView.visibility = View.GONE
        if (content.isNullOrBlank() || content?.trim() == "N/A") {
            header.visibility = View.GONE
            return
        }

        contentView.text = content
        isExpanded = false

        val listener = View.OnClickListener {
            if (isExpanded) {
                collapseEvent?.track()
            } else {
                expandEvent?.track()
            }

            contentView.visibility = if (isExpanded) View.GONE else View.VISIBLE
            toggleButton.rotation = if (isExpanded) 0f else 180f
            isExpanded = !isExpanded
        }

        toggleButton.setOnClickListener(listener)
        header.setOnClickListener(listener)
    }

}