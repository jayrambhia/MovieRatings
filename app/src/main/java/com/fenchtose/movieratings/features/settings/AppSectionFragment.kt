package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences

class AppSectionFragment: BaseFragment() {

    private var preferences: UserPreferences? = null
    private var ratingDurationView: TextView? = null
    private var updatePublisher: PreferenceUpdater? = null

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.settings_app_section_page_header
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_app_section_page_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val preferences = SettingsPreferences(context)
        this.preferences = preferences

        addAppToggle(preferences, view, R.id.netflix_toggle, UserPreferences.NETFLIX)
        addAppToggle(preferences, view, R.id.prime_video_toggle, UserPreferences.PRIMEVIDEO)
        addAppToggle(preferences, view, R.id.play_movies_toggle, UserPreferences.PLAY_MOVIES)

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

    private fun addAppToggle(preferences: UserPreferences, root: View, @IdRes buttonId: Int, key: String) {
        val toggle = root.findViewById<SwitchCompat?>(buttonId)
        toggle?.let {
            it.visibility = View.VISIBLE
            it.isChecked = preferences.isAppEnabled(key)
            it.setOnCheckedChangeListener {
                _, isChecked -> updatePreference(preferences, key, isChecked)
            }
        }
    }

    private fun updatePreference(preferences: UserPreferences, app: String, checked: Boolean) {
        preferences.setAppEnabled(app, checked)
        updatePublisher?.show(app)
    }

    private fun updateRatingDisplayDuration(durationInMs: Int) {
        preferences?.let {
            it.setRatingDisplayDuration(durationInMs)
            updatePublisher?.show("toast")
            ratingDurationView?.text = (it.getRatingDisplayDuration()/1000).toString()
        }
    }

    class SettingsAppSectionPath: RouterPath<AppSectionFragment>() {
        override fun createFragmentInstance(): AppSectionFragment {
            return AppSectionFragment()
        }
    }
}