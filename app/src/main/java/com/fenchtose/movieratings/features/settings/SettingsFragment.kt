package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.settings.bubble.RatingBubbleSectionFragment
import com.fenchtose.movieratings.util.checkBatteryOptimized
import com.fenchtose.movieratings.util.show

class SettingsFragment : BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_header
    override fun screenName() = GaScreens.SETTINGS

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_page_redesign_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindAction(view, R.id.settings_app_section, AppSectionFragment.SettingsAppSectionPath())
        bindAction(view, R.id.settings_data_section, DataSectionFragment.DataSettingsPath())
        bindAction(view, R.id.settings_tts_section, TTSSectionFragment.TTSSettingsPath())
        bindAction(view, R.id.settings_misc_section, MiscSectionFragment.MiscSettingsPath())
        bindAction(view, R.id.settings_rating_section, RatingBubbleSectionFragment.RatingSectionPath())
        if (checkBatteryOptimized(requireContext())) {
            bindAction(view, R.id.section_battery_optimization, BatteryOptimizationPath())
        }
    }

    private fun bindAction(root: View, @IdRes id: Int, goTo: RouterPath<out BaseFragment>) {
        root.findViewById<View?>(id)?.apply {
            show(true)
            setOnClickListener {
                path?.getRouter()?.go(goTo)
            }
        }
    }

    class SettingsPath: RouterPath<SettingsFragment>() {
        override fun createFragmentInstance() = SettingsFragment()
        override fun category() = GaCategory.SETTINGS
    }
}