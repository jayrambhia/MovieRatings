package com.fenchtose.movieratings.features.info

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.accessinfo.AccessInfoFragment
import com.fenchtose.movieratings.features.debugging.DebugOptionsPath
import com.fenchtose.movieratings.features.premium.DonatePageFragment
import com.fenchtose.movieratings.features.settings.SettingsFragment
import com.fenchtose.movieratings.model.inAppAnalytics.DbHistoryKeeper
import com.fenchtose.movieratings.util.*

class InfoPageBottomView: LinearLayout {

    private var router: Router? = null
    private var navController: NavController? = null

    private var category: String = "unknown"

    private var accessibilityButton: View? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.info_page_bottom_container_layout, this, true)
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))

        val historyKeeper = DbHistoryKeeper.newInstance(MovieRatingsApplication.instance!!)
        accessibilityButton = findViewById(R.id.activate_button)
        accessibilityButton?.setOnClickListener {
            AppEvents.ACTIVATE_FLUTTER.track()
            router?.go(AccessInfoFragment.AccessibilityPath())
        }

        val premiumView: View = findViewById(R.id.premium_view)
        if (BuildConfig.FLAVOR == "playstore") {
            premiumView.visibility = View.VISIBLE
            premiumView.setOnClickListener {
                AppEvents.openSupportApp(category).track()
                router?.go(DonatePageFragment.DonatePath())
            }
        }

        findViewById<View>(R.id.credit_view).setOnClickListener {
            showCreditsDialog()
        }

        findViewById<View>(R.id.rate_view).setOnClickListener {
            AppEvents.rateApp(category).track()
            historyKeeper.ratedAppOnPlaystore()
            IntentUtils.openPlaystore(context)
        }

        val share = findViewById<View>(R.id.share_view)
        share?.let {
            share.setOnClickListener {
                AppEvents.shareApp(category).track()
                IntentUtils.openShareIntent(context, context.getString(R.string.info_page_share_content, Constants.APP_SHARE_URL))
            }
        }

        val settingsView = findViewById<View?>(R.id.settings_view)
        settingsView?.setOnClickListener {
            AppEvents.openSettings(category).track()
            router?.go(SettingsFragment.SettingsPath())
            navController?.navigate(R.id.settings)
        }

        findViewById<View?>(R.id.feedback_view)?.setOnClickListener {
            AppEvents.reportBug(category).track()
            IntentUtils.openReportBugIntent(context, "\n\n\n\n\n------\nThis data will help us in figuring out the issue.\n\n" +
                    "${getDeviceInfo()}\n\n${getAppInfo(context)}")
        }

        findViewById<View?>(R.id.privacy_policy_view)?.setOnClickListener {
            AppEvents.openPrivacyPolicy(category).track()
            IntentUtils.openPrivacyPolicy(context)
        }

        findViewById<View?>(R.id.debug_view)?.setOnClickListener {
            router?.go(DebugOptionsPath())
        }
    }

    fun setRouter(router: Router?, category: String?, navController: NavController?) {
        this.router = router
        this.category = category ?: "unknown"
        this.navController = navController
    }

    private fun showCreditsDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.credit_dialog_title)
                .setMessage(R.string.credit_dialog_content)
                .show()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        accessibilityButton?.show(!AccessibilityUtils.isAccessibilityEnabled(context))
    }
}