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
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.isNotificationChannelBlocked
import com.fenchtose.movieratings.util.showReviewAppNotification
import com.fenchtose.movieratings.util.showSupportAppNotification

class DebugOptionsFragment: BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.debug_screen_title
    override fun screenName() =  GaScreens.DEBUGGING

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.debug_options_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.debug_notification_support).setOnClickListener {
            sendSupportAppNotification()
        }

        view.findViewById<View>(R.id.debug_notification_rate).setOnClickListener {
            sendRateAppNotification()
        }
    }

    private fun sendSupportAppNotification() {
        if (!isNotificationChannelBlocked(requireContext(), Constants.SUPPORT_CHANNEL_ID)) {
            showSupportAppNotification(requireContext(), GaCategory.DEBUGGING)
            return
        }

        showSnackbar("notification channel is blocked")
    }

    private fun sendRateAppNotification() {
        if (!isNotificationChannelBlocked(requireContext(), Constants.SUPPORT_CHANNEL_ID)) {
            showReviewAppNotification(requireContext(), GaCategory.DEBUGGING)
            return
        }

        showSnackbar("notification channel is blocked")
    }
}

class DebugOptionsPath: RouterPath<DebugOptionsFragment>() {
    override fun createFragmentInstance(): DebugOptionsFragment {
        return DebugOptionsFragment()
    }
}