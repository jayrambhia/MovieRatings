package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences

class NotificationSectionFragment : BaseFragment() {

    private var helper: SettingsHelper? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.notification_settings_screen_title
    override fun screenName() = AppScreens.SETTINGS_NOTIFICATION_SECTION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_notificaiton_section_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preferences = SettingsPreferences(requireContext())

        helper?.clear()

        val helper = SettingsHelper(preferences, view) { _, _ ->
            showSnackbar(R.string.settings_preference_update_content)
        }

        helper.addAppToggle(R.id.support_review_toggle, UserPreferences.SHOW_RATE_APP_PROMPT)
        helper.addAppToggle(R.id.support_inapp_toggle, UserPreferences.SHOW_SUPPORT_APP_PROMPT)

        this.helper = helper
    }

    override fun onDestroyView() {
        super.onDestroyView()
        helper?.clear()
        helper = null
    }
}

class NotificationSectionPath : RouterPath<NotificationSectionFragment>() {
    override fun createFragmentInstance(): NotificationSectionFragment {
        return NotificationSectionFragment()
    }
}