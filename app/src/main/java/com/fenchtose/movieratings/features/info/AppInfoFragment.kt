package com.fenchtose.movieratings.features.info

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.IntentUtils

class AppInfoFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.info_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById(R.id.rate_view).setOnClickListener {
            IntentUtils.openPlaystore(context)
        }

        view.findViewById(R.id.share_view).setOnClickListener {
            IntentUtils.openShareIntent(context, "Have you met Flutter? It shows movie ratings on your screen when you're browsing Netflix. Get the app. " + IntentUtils.PLAYSTORE_URL)
        }

        view.findViewById(R.id.credit_view).setOnClickListener {
            showCreditsDialog()
        }

        (view.findViewById(R.id.version_view) as TextView).text = BuildConfig.VERSION_NAME
        (view.findViewById(R.id.info_content_view) as TextView)
                .setText(
                if (AccessibilityUtils.hasAllPermissions(context))
                    R.string.info_screen_content_with_accessibility
                else
                    R.string.info_screen_content_no_accessibility)
    }

    private fun showCreditsDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.credit_dialog_title)
                .setMessage(R.string.credit_dialog_content)
                .show()
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.search_page_title
    }

    class AppInfoPath: RouterPath<AppInfoFragment>() {
        override fun createFragmentInstance(): AppInfoFragment {
            return AppInfoFragment()
        }
    }
}