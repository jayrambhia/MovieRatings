package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.PackageUtils

class AppSectionFragment: BaseFragment() {

    private var preferences: UserPreferences? = null
    private var updatePublisher: PreferenceUpdater? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_app_section_page_header
    override fun screenName() = AppScreens.SETTINGS_APPS_SECTION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_app_section_page_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val preferences = SettingsPreferences(requireContext())
        this.preferences = preferences

        val toggleContainer = view.findViewById<ViewGroup>(R.id.toggles_container)
        setupAppToggles(preferences, view as ViewGroup, toggleContainer)

        updatePublisher = PreferenceUpdater(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.release()
    }

    private fun setupAppToggles(preferences: UserPreferences, root:ViewGroup, container: ViewGroup) {
        val notInstalledApps = ArrayList<String>()
        val installedApps = ArrayList<Pair<String, String>>()

        Constants.supportedApps.forEach {
            entry ->
            if (PackageUtils.isPackageInstalled(requireContext(), entry.key)) {
                installedApps.add(Pair(requireContext().getString(entry.value), entry.key))
            } else {
                notInstalledApps.add(requireContext().getString(entry.value))
            }
        }

        installedApps.sortedBy { it.first }
                .forEach {
                    addAppToggle(preferences, container, it.second, it.first)
                }

        val notInstalledAppsView = root.findViewById<TextView>(R.id.not_installed_apps)
        if (notInstalledApps.isEmpty()) {
            notInstalledAppsView.visibility = View.GONE
        } else {
            notInstalledAppsView.visibility = View.VISIBLE
            notInstalledAppsView.text = (requireContext().getString(R.string.settings_not_installed_apps, notInstalledApps.joinToString { it }))
        }
    }

    private fun addAppToggle(preferences: UserPreferences, container: ViewGroup, key: String, title: String) {
        val toggle = SwitchCompat(requireContext())
        toggle.setPadding(requireContext().resources.getDimensionPixelSize(R.dimen.gutter))
        toggle.setTextSize(TypedValue.COMPLEX_UNIT_PX, requireContext().resources.getDimension(R.dimen.settings_text_size))
        toggle.isChecked = preferences.isAppEnabled(key)
        toggle.text = title
        toggle.setOnCheckedChangeListener {
            _, isChecked -> updatePreference(preferences, key, isChecked)
        }

        container.addView(toggle)
    }

    private fun updatePreference(preferences: UserPreferences, app: String, checked: Boolean) {
        preferences.setEnabled(app, checked)
        updatePublisher?.show(app)
    }

    class SettingsAppSectionPath: RouterPath<AppSectionFragment>() {
        override fun createFragmentInstance(): AppSectionFragment {
            return AppSectionFragment()
        }
    }
}