package com.fenchtose.movieratings

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.os.ConfigurationCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.base.RouterBaseActivity
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.accessinfo.AccessInfoFragment
import com.fenchtose.movieratings.features.info.AppInfoFragment
import com.fenchtose.movieratings.features.searchpage.SearchPageFragment
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.inAppAnalytics.DbHistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.HistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.PreferenceUserHistory
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.IntentUtils
import com.fenchtose.movieratings.util.PackageUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject


class MainActivity : RouterBaseActivity() {

    private var container: FrameLayout? = null
    private var activateButton: ViewGroup? = null

    private var accessibilityPublisher: PublishSubject<Boolean>? = null
    private var accessibilityPagePublisher: PublishSubject<Boolean>? = null
    private var disposable: Disposable? = null

    private var analytics: AnalyticsDispatcher? = null

    private var preferences: UserPreferences? = null
    private var historyKeeper: HistoryKeeper? = null

    private val TTS_REG_CHECK = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.fragment_container)
        activateButton = findViewById(R.id.activate_button)

        preferences = SettingsPreferences(this)
        setupObservables()

        activateButton?.setOnClickListener {
            GaEvents.TAP_ACTIVATE_FLUTTER.track()
            showAccessibilityInfo()
        }

        accessibilityPagePublisher?.onNext(false)

        analytics = MovieRatingsApplication.analyticsDispatcher

        historyKeeper = DbHistoryKeeper(
                PreferenceUserHistory(this),
                DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao()),
                preferences!!)

        historyKeeper?.appOpened()

        checkForTTS()
        checkForLocale()

        initializeRouter(
                findViewById(R.id.toolbar),
                {
                    when(it) {
                        is AccessInfoFragment.AccessibilityPath -> accessibilityPagePublisher?.onNext(true)
                    }
                },
                {
                    when(it) {
                        is AccessInfoFragment.AccessibilityPath -> accessibilityPagePublisher?.onNext(false)
                    }
                },
                ::buildPathAndStart
        )
    }

    override fun onResume() {
        super.onResume()
        triggerAccessibilityCheck()
    }

    fun triggerAccessibilityCheck() {
        accessibilityPublisher?.onNext(AccessibilityUtils.hasAllPermissions(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityPublisher?.onComplete()
        accessibilityPagePublisher?.onComplete()
        disposable?.dispose()
    }

    private fun showAccessibilityInfo() {
        analytics?.sendEvent(Event("activate_button_clicked"))
        getRouter()?.go(AccessInfoFragment.AccessibilityPath())
    }

    private fun onAccessibilityActivated() {
        getRouter()?.onBackRequested()
        // Show a dialog?
        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.accessibility_enabled_dialog_title)
                .setMessage(R.string.accessibility_enabled_dialog_content)
                .setNeutralButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }

        if (PackageUtils.hasInstalled(this, PackageUtils.NETFLIX)) {
            builder.setPositiveButton(R.string.accessibility_enabled_open_netflix) { dialog, _ ->
                dialog.dismiss()
                IntentUtils.launch3rdParty(this, PackageUtils.NETFLIX)
            }
        }
    }

    private fun setupObservables() {
        accessibilityPublisher = PublishSubject.create()
        accessibilityPagePublisher = PublishSubject.create()

        disposable =
                Observable.combineLatest(accessibilityPublisher, accessibilityPagePublisher,
                BiFunction<Boolean, Boolean, Int> {
                    hasAccessibility, isShowingAccessibilityInfo ->
                    when {
                        hasAccessibility && isShowingAccessibilityInfo -> 1
                        !hasAccessibility && !isShowingAccessibilityInfo -> 2
                        else -> 3
                    }

                })
                .map {
                      preferences?.let {
                          if (!it.isAppEnabled(UserPreferences.SHOW_ACTIVATE_FLUTTER)) {
                              return@map 3
                          }
                      }

                    it
                }
                .subscribeBy(
                        onNext = {
                            when(it) {
                                1 -> onAccessibilityActivated()
                                2 -> activateButton?.visibility = View.VISIBLE
                                3 -> activateButton?.visibility = View.GONE
                            }
                        }
                )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            TTS_REG_CHECK -> {
                preferences?.setEnabled(UserPreferences.TTS_AVAILABLE, resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            }
        }
    }

    private fun checkForTTS() {
        val intent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        if (PackageUtils.isIntentCallabale(this, intent)) {
            startActivityForResult(intent, TTS_REG_CHECK)
        } else {
            preferences?.setEnabled(UserPreferences.TTS_AVAILABLE, false)
            Log.e("Flutter", "TTS is not supported")
        }
    }

    private fun checkForLocale() {
        val locales = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
        if (locales.size() > 0) {
            if (!locales[0].toLanguageTag().startsWith("en") && preferences?.isSettingEnabled(UserPreferences.LOCALE_INFO_SHOWN) == false) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.locale_info_dialog_title)
                        .setMessage(R.string.locale_info_dialog_content)
                        .setPositiveButton(android.R.string.ok) {
                            dialog, _ ->
                                preferences?.setEnabled(UserPreferences.LOCALE_INFO_SHOWN, true)
                                dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
            }
        }
    }

    private fun buildPathAndStart(router: Router) {
        val intent = intent
        var history: Router.History = Router.History()

        if (intent != null) {
            val historyBundle = intent.getBundleExtra(Router.HISTORY)
            if (historyBundle != null) {
                history = Router.History(historyBundle)
            }
        }

        if (!history.isEmpty()) {
            router.buildRoute(SearchPageFragment.SearchPath.Default(SettingsPreferences(this)))
            val grouped = history.history.groupBy { router.canHandleKey(it.first) }
            grouped[true]?.map {
                router.buildRoute(it.second)
            }

            val redirects = grouped[false]
            redirects?.let {
                if (it.isNotEmpty()) {
                    // Take the first key at the moment. We don't have the logic right now
                    when(it[0].first) {
                        "RateApp" -> {
                            historyKeeper?.ratedAppOnPlaystore()
                            IntentUtils.openPlaystore(this)
                            supportFinishAfterTransition()
                            return
                        }
                    }
                }
            }
        } else {
            if (preferences?.isSettingEnabled(UserPreferences.ONBOARDING_SHOWN) == false && !AccessibilityUtils.hasAllPermissions(this)) {
                router.buildRoute(AppInfoFragment.AppInfoPath(true))
                preferences?.setEnabled(UserPreferences.ONBOARDING_SHOWN, true)
            } else {
                router.buildRoute(SearchPageFragment.SearchPath.Default(SettingsPreferences(this)))
            }
        }

        router.start()
    }

}
