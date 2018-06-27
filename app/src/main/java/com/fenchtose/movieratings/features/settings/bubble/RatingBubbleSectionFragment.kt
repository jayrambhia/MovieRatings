package com.fenchtose.movieratings.features.settings.bubble

import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.settings.PreferenceUpdater
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.widgets.RatingBubble

class RatingBubbleSectionFragment: BaseFragment() {

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_rating_bubble_section_page_header

    private var preferences: UserPreferences? = null
    private var adapter: BubbleColorAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var ratingBubble: RatingBubble? = null
    private var ratingDurationView: TextView? = null
    private var updatePublisher: PreferenceUpdater? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.settings_rating_bubble_section_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = SettingsPreferences(context)
        this.preferences = preferences

        val savedBubbleColor = preferences.getBubbleColor(ContextCompat.getColor(context, R.color.floating_rating_color))

        adapter = BubbleColorAdapter(context, context.resources.getIntArray(R.array.bubble_dark).union(context.resources.getIntArray(R.array.bubble_light).asIterable()).toList(),
                savedBubbleColor, object: BubbleColorAdapter.ColorSelectorCallback {
            override fun onColorSelected(color: Int, position: Int) {
                adapter?.selected = position
                adapter?.notifyDataSetChanged()
                updateBubbleColor(color)
                recyclerView?.let {
                    moveRecyclerViewToCenter(it, position, adapter!!.itemCount)
                }
            }
        })

        adapter?.setHasStableIds(true)

        val recyclerView = view.findViewById<RecyclerView>(R.id.bubble_color_recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        this.recyclerView = recyclerView

        ratingBubble = view.findViewById(R.id.rating_bubble)
        ratingBubble?.setText("Flutter ratings")
        ratingBubble?.updateColor(savedBubbleColor)

        val ratingDurationSeekbar = view.findViewById<SeekBar>(R.id.rating_duration_seekbar)
        ratingDurationView = view.findViewById(R.id.rating_duration_view)

        val progress = preferences.getRatingDisplayDuration()/1000
        ratingDurationView?.text = (progress).toString()
        ratingDurationSeekbar?.progress = progress - 1

        ratingDurationSeekbar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateRatingDisplayDuration((progress + 1) * 1000)
            }
        })

        updatePublisher = PreferenceUpdater(view as ViewGroup)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.release()
    }

    private fun updateBubbleColor(@ColorInt color: Int) {
        ratingBubble?.updateColor(color)
        preferences?.setBubbleColor(color)
//        updatePublisher?.show("color")
    }

    private fun updateRatingDisplayDuration(durationInMs: Int) {
        preferences?.let {
            it.setRatingDisplayDuration(durationInMs)
//            updatePublisher?.show("toast")
            ratingDurationView?.text = (it.getRatingDisplayDuration()/1000).toString()
        }
    }

    private fun moveRecyclerViewToCenter(recyclerView: RecyclerView, position: Int, total: Int) {
        val manager = recyclerView.layoutManager as LinearLayoutManager
        val first = manager.findFirstVisibleItemPosition()
        if (first >= position - 2) {
            manager.scrollToPosition(Math.max(position - 2, 0))
        } else {
            manager.scrollToPosition(Math.min(position + 2, total))
        }
    }

    class RatingSectionPath: RouterPath<RatingBubbleSectionFragment>() {
        override fun createFragmentInstance(): RatingBubbleSectionFragment {
            return RatingBubbleSectionFragment()
        }
    }

}