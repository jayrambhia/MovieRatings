package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences

class MiscSectionFragment : BaseFragment() {

    private var helper: SettingsHelper? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_misc_section_page_header
    override fun screenName() = GaScreens.SETTINGS_MISC_SECTION
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_misc_section_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = SettingsPreferences(requireContext())
        helper?.clear()

        val helper = SettingsHelper(preferences, view) { _, _ ->
            showSnackbar(R.string.settings_preference_update_content)
        }

        helper.addAppToggle(R.id.use_year_toggle, UserPreferences.USE_YEAR)
        helper.addAppToggle(R.id.api_fallback_toggle, UserPreferences.USE_FLUTTER_API)
        helper.addSettingToggle(R.id.api_order_toggle, UserPreferences.SHOW_RECENT_RATING)
        helper.addAppToggle(R.id.anime_toggle, UserPreferences.CHECK_ANIME)

        this.helper = helper
    }

    override fun onDestroyView() {
        super.onDestroyView()
        helper?.clear()
        helper = null
    }

    class MiscSettingsPath : RouterPath<MiscSectionFragment>() {
        override fun createFragmentInstance(): MiscSectionFragment {
            return MiscSectionFragment()
        }
    }
}

