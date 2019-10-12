package com.fenchtose.movieratings.features.settings.bubble

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.EventBus
import com.fenchtose.movieratings.features.settings.SettingsHelper
import com.fenchtose.movieratings.features.stickyview.BubbleSize
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.*
import com.fenchtose.movieratings.widgets.ViewGrouper

class RatingBubbleSectionFragment: BaseFragment() {

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_rating_bubble_section_page_header
    override fun screenName() = GaScreens.SETTINGS_RATING_SECTION

    private var adapter: BubbleColorAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var drawPermissionConainer: View? = null
    private var seekbarGroup: ViewGrouper? = null
    private var ratingDurationView: TextView? = null

    private var settingsHelper: SettingsHelper? = null
    private var preferences: UserPreferences? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.settings_rating_bubble_section_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = SettingsPreferences(requireContext())
        this.preferences = preferences
        this.settingsHelper = SettingsHelper(preferences, view) { key, value ->
            showSnackbar(R.string.settings_preference_update_content)
            if (key == UserPreferences.RATING_DETAILS) {
                EventBus.send(BubbleDetailEvent(if (value) BubbleSize.BIG else BubbleSize.SMALL))
            }
        }

        drawPermissionConainer = view.findViewById(R.id.draw_permission_container)
        view.findViewById<View>(R.id.draw_permission_cta).setOnClickListener {
            if (VersionUtils.isMOrAbove()) {
                openDrawSettings()
            }
        }

        settingsHelper?.addAppToggle(R.id.open_movie_toggle, UserPreferences.OPEN_MOVIE_IN_APP)
        settingsHelper?.addAppToggle(R.id.rating_details_toggle, UserPreferences.RATING_DETAILS)
        settingsHelper?.addSettingToggle(R.id.open_404_toggle, UserPreferences.OPEN_404)

        val savedBubbleColor = preferences.getBubbleColor(ContextCompat.getColor(requireContext(), R.color.floating_rating_color))

        adapter = BubbleColorAdapter(requireContext(), requireContext().resources.getIntArray(R.array.bubble_dark).union(requireContext().resources.getIntArray(R.array.bubble_light).asIterable()).toList(),
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
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        this.recyclerView = recyclerView

        seekbarGroup = ViewGrouper(
            view.findViewById(R.id.rating_duration_info),
            view.findViewById(R.id.seekbar_container),
            view.findViewById(R.id.duration_divider)
        )

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

        requireActivity().startService(Intent(context, BubbleService::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (VersionUtils.isMOrAbove() && !AccessibilityUtils.isTV(requireContext())) {
            if (!AccessibilityUtils.isDrawPermissionEnabled(requireContext())) {
                drawPermissionConainer?.show(true)
                seekbarGroup?.show(false)
            } else {
                drawPermissionConainer?.show(false)
                seekbarGroup?.show(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        settingsHelper?.clear()
        settingsHelper = null
        requireActivity().stopService(Intent(context, BubbleService::class.java))
    }

    private fun updateBubbleColor(@ColorInt color: Int) {
        preferences?.setBubbleColor(color)
        EventBus.send(BubbleColorEvent(color))
    }

    private fun updateRatingDisplayDuration(durationInMs: Int) {
        preferences?.let {
            it.setRatingDisplayDuration(durationInMs)
            settingsHelper?.dispatch("toast", true)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openDrawSettings() {
        if (!IntentUtils.openDrawSettings(requireContext())) {
            showSnackbar(R.string.accessibility_draw_permission_launch_error)
        }
    }

    class RatingSectionPath: RouterPath<RatingBubbleSectionFragment>() {
        override fun createFragmentInstance(): RatingBubbleSectionFragment {
            return RatingBubbleSectionFragment()
        }
    }

}