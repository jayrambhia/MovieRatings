package com.fenchtose.movieratings.features.info

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.IntentUtils

class AppInfoFragment: BaseFragment() {

    private var analytics: AnalyticsDispatcher? = null
    private var isTV: Boolean = false
    private var testView: View? = null
    private var testContainer: View? = null
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTV = AccessibilityUtils.isTV(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.info_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics = MovieRatingsApplication.getAnalyticsDispatcher()

        view.findViewById(R.id.rate_view).setOnClickListener {
            analytics?.sendEvent(Event("rate_app_clicked"))
            IntentUtils.openPlaystore(context)
        }

        val share = view.findViewById(R.id.share_view)
        share?.let {
            share.setOnClickListener {
                analytics?.sendEvent(Event("share_app_clicked"))
                IntentUtils.openShareIntent(context, "Have you met Flutter? It shows movie ratings on your screen when you're browsing Netflix. Get the app. https://goo.gl/y3HXVi")
            }
        }

        testContainer = view.findViewById(R.id.test_container)
        handler = Handler()

        testView = view.findViewById(R.id.test_view)
        testView?.let {
            testView!!.setOnClickListener {
                handler?.postDelayed({
                    testContainer?.visibility = VISIBLE
                    handler?.postDelayed({
                        testContainer?.visibility = GONE
                    }, 3000)
                }, 30)
            }
        }

//        if (isTV) {
//            share.visibility = GONE
//        }

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

    override fun onResume() {
        super.onResume()
        if (isTV) {
            val hasAccessibility = AccessibilityUtils.isAccessibilityEnabled(context)
            testView?.visibility = if (hasAccessibility) VISIBLE else GONE
        }
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