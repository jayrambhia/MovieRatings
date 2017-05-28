package com.fenchtose.movieratings.features.access_info

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath

class AccessInfoFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.access_info_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById(R.id.settings_button).setOnClickListener {
            openSettings()
        }

        val infoView = view.findViewById(R.id.info_view) as TextView
        infoView.text = getString(R.string.accessibility_access_info_content,
                getString(R.string.accessibility_info_app_name),
                getString(R.string.accessibility_info_target_name))
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): String {
        return "Accessibility Information"
    }

    class AccessibilityPath : RouterPath<AccessInfoFragment>() {
        override fun createFragmentInstance(): AccessInfoFragment {
            return AccessInfoFragment()
        }
    }
}