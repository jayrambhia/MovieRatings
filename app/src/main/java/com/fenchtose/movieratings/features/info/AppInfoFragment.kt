package com.fenchtose.movieratings.features.info

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.show

class AppInfoFragment: BaseFragment() {

    private var isTV: Boolean = false
    private var testView: View? = null
    private var testContainer: View? = null
    private var activationWarning: View? = null
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTV = AccessibilityUtils.isTV(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(if (isTV) R.layout.info_page_layout_tv else R.layout.info_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testContainer = view.findViewById(R.id.test_container)
        handler = Handler()

        testView = view.findViewById(R.id.test_view)
        testView?.let {
            it.setOnClickListener {
                handler?.postDelayed({
                    testContainer?.visibility = VISIBLE
                    handler?.postDelayed({
                        testContainer?.visibility = GONE
                    }, 3000)
                }, 30)
            }
        }

        activationWarning = view.findViewById(R.id.activation_warning_view)

        view.findViewById<TextView>(R.id.version_view).text = BuildConfig.VERSION_NAME

        val contentView = view.findViewById<TextView?>(R.id.info_content_view)
        contentView?.visibility = View.VISIBLE
        contentView?.setText(
                if (AccessibilityUtils.isAccessibilityEnabled(requireContext()))
                    R.string.info_screen_content_with_accessibility
                else
                    R.string.info_screen_content_no_accessibility)

        view.findViewById<InfoPageBottomView>(R.id.bottom_container).apply {
            setRouter(path?.getRouter(), path?.category())
            findViewById<View?>(R.id.rate_view)?.show(false)
            findViewById<View?>(R.id.premium_view)?.show(false)
            findViewById<View?>(R.id.feedback_view)?.show(true)
            findViewById<View?>(R.id.privacy_policy_view)?.show(true)
            findViewById<View?>(R.id.debug_view)?.show(BuildConfig.DEBUG)
        }
    }

    override fun onDestroyView() {
        view?.findViewById<InfoPageBottomView>(R.id.bottom_container)?.setRouter(null, null)
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        if (isTV) {
            val hasAccessibility = AccessibilityUtils.isAccessibilityEnabled(requireContext())
            testView?.visibility = if (hasAccessibility) VISIBLE else GONE
            activationWarning?.visibility = if (hasAccessibility) GONE else VISIBLE
        }
    }

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.search_page_title
    override fun screenName() = AppScreens.APP_INFO

    class AppInfoPath: RouterPath<AppInfoFragment>() {
        override fun createFragmentInstance() =AppInfoFragment()
        override fun category() = GaCategory.APP_INFO
    }
}