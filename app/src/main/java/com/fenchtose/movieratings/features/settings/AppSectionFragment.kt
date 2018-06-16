package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.v7.widget.SwitchCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.view.setPadding
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.PackageUtils

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

        val toggleContainer = view.findViewById<ViewGroup>(R.id.toggles_container)
        setupAppToggles(preferences, view as ViewGroup, toggleContainer)

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

        updatePublisher = PreferenceUpdater(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.release()
    }

    private fun setupAppToggles(preferences: UserPreferences, root:ViewGroup, container: ViewGroup) {
        val notInstalledApps = ArrayList<String>()

        Constants.supportedApps.forEach {
            entry ->
            if (PackageUtils.isPackageInstalled(context, entry.key)) {
                addAppToggle(preferences, container, entry.key, context.getString(entry.value))
            } else {
                notInstalledApps.add(context.getString(entry.value))
            }
        }

        val notInstalledAppsView = root.findViewById<TextView>(R.id.not_installed_apps)
        if (notInstalledApps.isEmpty()) {
            notInstalledAppsView.visibility = View.GONE
        } else {
            notInstalledAppsView.visibility = View.VISIBLE
            notInstalledAppsView.text = (context.getString(R.string.settings_not_installed_apps, notInstalledApps.joinToString { it }))
        }
    }

    private fun addAppToggle(preferences: UserPreferences, container: ViewGroup, key: String, title: String) {
        val toggle = SwitchCompat(context)
        toggle.setPadding(context.resources.getDimensionPixelSize(R.dimen.gutter))
        toggle.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.settings_text_size))
        toggle.isChecked = preferences.isAppEnabled(key)
        toggle.text = title
        toggle.setOnCheckedChangeListener {
            _, isChecked -> updatePreference(preferences, key, isChecked)
        }

        container.addView(toggle)
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