package com.fenchtose.movieratings.features.access_info

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.NetflixReaderService
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.VersionUtils

class AccessInfoFragment : BaseFragment() {

    private var accessContainer: View? = null
    private var drawContainer: View? = null
    private var analytics: AnalyticsDispatcher? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.access_info_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accessContainer = view.findViewById(R.id.access_container)
        drawContainer = view.findViewById(R.id.draw_container)

        view.findViewById(R.id.settings_button).setOnClickListener {
            analytics?.sendEvent(Event("open_accessibility_settings"))
            openSettings()
        }

        val infoView = view.findViewById(R.id.info_view) as TextView
        infoView.text = getString(R.string.accessibility_access_info_content,
                getString(R.string.accessibility_info_app_name),
                getString(R.string.accessibility_info_target_name))

        view.findViewById(R.id.draw_settings_button).setOnClickListener {
            if (VersionUtils.isMOrAbove()) {
                analytics?.sendEvent(Event("open_draw_permissions_settings"))
                openDrawSettings()
            }
        }

        val drawInfoView = view.findViewById(R.id.draw_info_view) as TextView
        drawInfoView.text = getString(R.string.draw_access_info_content,
                getString(R.string.accessibility_info_app_name),
                getString(R.string.accessibility_info_target_name))

        analytics = MovieRatingsApplication.getAnalyticsDispatcher()

    }

    override fun onResume() {
        super.onResume()
        drawContainer?.visibility =  if (!AccessibilityUtils.isDrawPermissionEnabled(context)) VISIBLE else GONE
        accessContainer?.visibility = if (!AccessibilityUtils.isAccessibilityEnabled(context,
                BuildConfig.APPLICATION_ID + "/." + NetflixReaderService::class.java.simpleName)) VISIBLE else GONE
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun openDrawSettings() {
        @SuppressLint("InlinedApi")
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName))
        startActivity(intent)
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.accessibility_info_page_header
    }

    class AccessibilityPath : RouterPath<AccessInfoFragment>() {
        override fun createFragmentInstance(): AccessInfoFragment {
            return AccessInfoFragment()
        }
    }
}