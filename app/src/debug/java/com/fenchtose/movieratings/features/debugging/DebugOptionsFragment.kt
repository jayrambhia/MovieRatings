package com.fenchtose.movieratings.features.debugging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.isNotificationChannelBlocked
import com.fenchtose.movieratings.util.showReviewAppNotification
import com.fenchtose.movieratings.util.showSupportAppNotification

class DebugOptionsFragment: BaseFragment() {

    private var preferences: UserPreferences? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.debug_screen_title
    override fun screenName() =  GaScreens.DEBUGGING

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.debug_options_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = SettingsPreferences(requireContext())

        view.findViewById<View>(R.id.debug_notification_support).setOnClickListener {
            sendSupportAppNotification()
        }

        view.findViewById<View>(R.id.debug_notification_rate).setOnClickListener {
            sendRateAppNotification()
        }
    }

    private fun sendSupportAppNotification() {
        val channelBlocked = isNotificationChannelBlocked(requireContext(), Constants.SUPPORT_CHANNEL_ID)
        val enabled = preferences?.isAppEnabled(UserPreferences.SHOW_SUPPORT_APP_PROMPT) == true
        if (!channelBlocked && enabled) {
            showSupportAppNotification(requireContext(), GaCategory.DEBUGGING)
            return
        } else if (channelBlocked) {
            showSnackbar("notification channel is blocked")
        } else {
            showSnackbar("Disabled in app settings")
        }
    }

    private fun sendRateAppNotification() {
        val channelBlocked = isNotificationChannelBlocked(requireContext(), Constants.SUPPORT_CHANNEL_ID)
        val enabled = preferences?.isAppEnabled(UserPreferences.SHOW_RATE_APP_PROMPT) == true
        if (!channelBlocked && enabled) {
            showReviewAppNotification(requireContext(), GaCategory.DEBUGGING)
            return
        } else if (channelBlocked) {
            showSnackbar("notification channel is blocked")
        } else {
            showSnackbar("Disabled in app settings")
        }
    }
}

class DebugOptionsPath: RouterPath<DebugOptionsFragment>() {
    override fun createFragmentInstance(): DebugOptionsFragment {
        return DebugOptionsFragment()
    }
}