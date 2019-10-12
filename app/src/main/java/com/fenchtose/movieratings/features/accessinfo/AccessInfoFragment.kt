package com.fenchtose.movieratings.features.accessinfo

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.*

class AccessInfoFragment : BaseFragment() {

    private var accessContainer: View? = null
    private var drawContainer: View? = null
    private var enabledContainer: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.access_info_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accessContainer = view.findViewById(R.id.access_container)
        drawContainer = view.findViewById(R.id.draw_container)
        enabledContainer = view.findViewById(R.id.enabled_container)

        view.findViewById<View>(R.id.settings_button).setOnClickListener {
            openSettings()
        }

        val infoView = view.findViewById<TextView>(R.id.info_view)
        infoView.text = getString(R.string.accessibility_access_info_content,
                getString(R.string.accessibility_info_app_name),
                getString(R.string.accessibility_info_target_name))

        view.findViewById<View>(R.id.draw_settings_button).setOnClickListener {
            if (VersionUtils.isMOrAbove()) {
                openDrawSettings()
            }
        }

        view.findViewById<View>(R.id.netflix_cta).setOnClickListener {
            if (!IntentUtils.launch3rdParty(requireActivity(), PackageUtils.NETFLIX)) {
                showSnackbar(R.string.accessibility_enabled_netflix_open_error)
            }
        }

        val drawInfoView: TextView = view.findViewById(R.id.draw_info_view)
        drawInfoView.text = getString(R.string.draw_access_info_content,
                getString(R.string.accessibility_info_app_name),
                getString(R.string.accessibility_info_target_name))

    }

    override fun onResume() {
        super.onResume()
        val accessibilityEnabled = AccessibilityUtils.isAccessibilityEnabled(requireContext())
        val drawPermission = AccessibilityUtils.isDrawPermissionEnabled(requireContext())
        drawContainer?.show(!drawPermission)
        accessContainer?.show(!accessibilityEnabled)
        enabledContainer?.show(accessibilityEnabled)
    }

    private fun openSettings() {
        if (!IntentUtils.openSettings(requireContext())) {
            showSnackbar(R.string.accessibility_settings_launch_error)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openDrawSettings() {
         if (!IntentUtils.openDrawSettings(requireContext())) {
            showSnackbar(R.string.accessibility_draw_permission_launch_error)
        }
    }

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.accessibility_info_page_header
    override fun screenName() = GaScreens.ACCESS_INFO

    class AccessibilityPath : RouterPath<AccessInfoFragment>() {
        override fun createFragmentInstance() = AccessInfoFragment()
        override fun showMenuIcons() = intArrayOf(R.id.action_settings)
        override fun category() = GaCategory.ACCESSIBILITY
    }
}