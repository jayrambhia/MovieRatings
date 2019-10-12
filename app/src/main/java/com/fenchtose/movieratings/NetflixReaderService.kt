package com.fenchtose.movieratings

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.analytics.ga.GaCategory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.GaLabels
import com.fenchtose.movieratings.display.RatingDisplayer
import com.fenchtose.movieratings.features.tts.Speaker
import com.fenchtose.movieratings.model.api.provider.MovieRatingsProvider
import com.fenchtose.movieratings.model.api.provider.ORDER_POPULAR
import com.fenchtose.movieratings.model.api.provider.ORDER_RECENT
import com.fenchtose.movieratings.model.api.provider.RatingRequest
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.inAppAnalytics.DbHistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.HistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.PreferenceUserHistory
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.reader.*
import com.fenchtose.movieratings.util.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class NetflixReaderService : AccessibilityService() {

    private var title: String? = null
    private val TAG: String = "NetflixReaderService"

    private var provider: MovieRatingsProvider? = null
    private val readersMap = HashMap<String, AppReader>()

    private var preferences: UserPreferences? = null

    // For Samsung S6 edge, we are getting TYPE_WINDOW_STATE_CHANGED for adding floating window which triggers removeView()
    private val supportedPackages = Constants.supportedApps.keys

    private var lastWindowStateChangeEventTime: Long = 0
    private val WINDOW_STATE_CHANGE_THRESHOLD = 2000

    private var displayer: RatingDisplayer? = null
    private var speaker: Speaker? = null

    private var historyKeeper: HistoryKeeper? = null

    private val RESOURCE_THRESHOLD = 300L

    private var resourceRemover: PublishSubject<Boolean>? = null

    private lateinit var requestPublisher: PublishSubject<RatingRequest>
    private lateinit var historyPublisher: PublishSubject<RatingRequest>
    private lateinit var myScheduler: Scheduler

    override fun onCreate() {
        super.onCreate()

        readersMap.clear()
        readersMap[Constants.PACKAGE_NETFLIX] = NetflixReader()
        readersMap[Constants.PACKAGE_PRIMEVIDEO] = PrimeVideoReader()
        readersMap[Constants.PACKAGE_HOTSTAR] = HotstarReader()
        readersMap[Constants.PACKAGE_BBC_IPLAYER] = BBCiPlayerReader()
        readersMap[Constants.PACKAGE_PLAY_MOVIES_TV] = PlayMoviesReader()
        readersMap[Constants.PACKAGE_JIO_CINEMA] = JioCinemaReader()
        readersMap[Constants.PACKAGE_JIO_TV] = JioTvReader()
        readersMap[Constants.PACKAGE_YOUTUBE] = YoutubeReader()
        readersMap[Constants.PACKAGE_REDBOX] = RedboxReader()
        readersMap[BuildConfig.APPLICATION_ID] = FlutterTestReader()

        preferences = SettingsPreferences(this)
        displayer = RatingDisplayer(this, preferences!!, analytics = true)

        myScheduler = AndroidSchedulers.from(Looper.myLooper())

        requestPublisher = PublishSubject.create()
        requestPublisher
                .doOnNext {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "get movie info: $it, ${System.currentTimeMillis()}")
                    }
                }
                .debounce(60, TimeUnit.MILLISECONDS)
                .doOnNext {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "get movie info after debounce: $it, ${System.currentTimeMillis()}")
                    }
                }
                .observeOn(myScheduler)
                .subscribe({
                    getMovieInfo(it)
                },{
                    it.printStackTrace()
                })

        historyPublisher = PublishSubject.create()
        historyPublisher.debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(myScheduler)
                .subscribe({
                    updateHistory(it.title, it.appName)
                }, {
                    it.printStackTrace()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPublisher.onComplete()
        historyPublisher.onComplete()
    }

    private fun initResources() {
        synchronized(this) {

            if (speaker == null && preferences?.isSettingEnabled(UserPreferences.USE_TTS) == true
                    && preferences?.isSettingEnabled(UserPreferences.TTS_AVAILABLE) == true) {
                speaker = Speaker(this)
            }

            if (provider == null) {
                provider = MovieRatingsApplication.ratingProviderModule.ratingProvider
            }

            if (historyKeeper == null) {
                historyKeeper = DbHistoryKeeper(PreferenceUserHistory(MovieRatingsApplication.instance),
                        DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao()),
                        preferences!!)
            }

            if (resourceRemover == null) {
                resourceRemover = PublishSubject.create()
                resourceRemover
                        ?.debounce(RESOURCE_THRESHOLD, TimeUnit.SECONDS)
                        ?.subscribe {
                            clearResources()
                        }
            }

            resourceRemover?.onNext(true)
        }
    }

    private fun clearResources() {
        synchronized(this) {
            speaker?.shutdown()
            speaker = null
            provider = null
            historyKeeper = null
            resourceRemover?.onComplete()
            resourceRemover = null
            System.gc()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "eventt: " + AccessibilityEvent.eventTypeToString(event.eventType) + ", " + event.packageName + ", " + event.action + " ${event.text}, ${event.className}\n$event")
        }

        if (!supportedPackages.contains(event.packageName)) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && displayer != null && displayer!!.isShowingView
                    && event.packageName != BuildConfig.APPLICATION_ID) {
                if (System.currentTimeMillis() - lastWindowStateChangeEventTime > WINDOW_STATE_CHANGE_THRESHOLD) {
                    // User has moved to some other app
                    displayer?.removeView()
                    title = null
                }
            }

            return
        }


        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            displayer?.removeView()
            lastWindowStateChangeEventTime = System.currentTimeMillis()
            title = null
        }

        val info = event.source ?: return
        val isAppEnabled = when (info.packageName) {
            BuildConfig.APPLICATION_ID -> true
            else -> preferences?.isAppEnabled(info.packageName.toString())
        }

        if (isAppEnabled == null || !isAppEnabled) {
            return
        }

        val reader = readersMap[info.packageName]
        if (reader != null) {
            val titles = reader.readTitles(event, info).distinctBy { it }
            val years = reader.readYear(info).distinctBy { it }
            if (BuildConfig.DEBUG) {
                if (titles.isEmpty()) {
                    checkNodeRecursively(info, 0)
                }
                Log.d(TAG, "scraped titles: $titles")
            }

            titles.firstOrNull { it != null && it.isNotBlank() }
                ?.let {
                    setMovieTitle(
                        fixTitle(info.packageName, it.toString()),
                        years.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                            fixYear(info.packageName, it.toString())
                        },
                        event.packageName.toString()
                    )
                }

        } else {
            checkNodeRecursively(info, 0)
        }
    }

    private fun checkNodeRecursively(info: AccessibilityNodeInfo?, level: Int) {
        if (!BuildConfig.DEBUG) {
            return
        }

        info?.let {

            Log.d("dump", "${" ".repeat(level)}info: text: ${it.text}, id: ${it.viewIdResourceName}, class: ${it.className}, parent id: ${it.parent?.viewIdResourceName}, desc=${it.contentDescription?.toString()}")
            if (info.childCount > 0) {
                Log.d("dump", "${" ".repeat(level)}--- <children> ---")
                (0 until info.childCount)
                        .forEach { index ->
                            checkNodeRecursively(it.getChild(index), level + 1)
                        }

                Log.d("dump", "${" ".repeat(level)}--- </children> ---")
            }
        }
    }

    override fun onInterrupt() {
    }

    private fun setMovieTitle(text: String, year: String?, appName: String) {

        // When the third condition is added, it could work better but could also be annoying because
        // event when the user scrolls, this would be triggered. This is just for Netflix because they
        // changed activity based navigation.

        if (title == null || title != text /*|| displayer?.isShowingView == false*/) {
            title = text
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Movie :- title: $text, year: $year")
            }

            displayer?.removeView()

            val request = RatingRequest(
                    title = text,
                    appName = appName,
                    order = if (preferences?.isSettingEnabled(UserPreferences.SHOW_RECENT_RATING) == true) ORDER_RECENT else ORDER_POPULAR,
                    checkAnime = if (preferences?.isAppEnabled(UserPreferences.CHECK_ANIME) == true) 1 else 0,
                    year = if (preferences?.isAppEnabled(UserPreferences.USE_YEAR) == true) year else null
            )

            requestPublisher.onNext(request)
        }
    }

    private fun fixTitle(@Suppress("UNUSED_PARAMETER") packageName: CharSequence, text: String): String {
        return FixTitleUtils.clean(text)
    }

    private fun fixYear(packageName: CharSequence, text: String?): String {
        return text?.let { return readersMap[packageName]?.fixYear(it) ?: "" } ?: ""
    }

    private fun getMovieInfo(request: RatingRequest) {
        initResources()

        val useFlutterApi = preferences?.isAppEnabled(UserPreferences.USE_FLUTTER_API) ?: true
        val provider = provider ?: return

        provider.useFlutterApi(useFlutterApi)
        provider.getMovieRating(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it.imdbId.isNotEmpty() }
            .doOnNext { historyPublisher.onNext(request) }
            .subscribe(::showRating) { error ->
                if (error is HttpException && error.code() == 404) {
                    AppEvents.ratingNotFound(
                        if (useFlutterApi) GaLabels.FLUTTER_API else GaLabels.OMDB_API
                    ).track()
                    update404(request.title, request.year)
                }

                if (BuildConfig.DEBUG) {
                    error.printStackTrace()
                }
            }
    }

    private fun updateHistory(imdbId: String, appName: String) {
        Observable.just(Pair(imdbId, appName))
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    Log.d(TAG, "update history")
                    historyKeeper?.ratingDisplayed(imdbId, appName)
                }
                .observeOn(myScheduler)
                .subscribe {
                    Log.d(TAG, "update history on next")
                    checkForSupportPrompt()
                }
    }

    private fun update404(title: String, year: String?) {
        provider?.report404(title, year)
        if (preferences?.isSettingEnabled(UserPreferences.OPEN_404) == true) {
            displayer?.show404(title)
        }
    }

    private fun checkForSupportPrompt() {
        historyKeeper?.let {
            it.shouldShowSupportAppPrompt()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it) {
                            showSupportAppPrompt()
                        } else {
                            checkForRateAppPrompt()
                        }
                    }
        }
    }

    private fun checkForRateAppPrompt() {
        historyKeeper?.let {
            it.shouldShowRateAppPrompt()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it) {
                            showRateAppPrompt()
                        }
                    }
        }
    }

    private fun showSupportAppPrompt() {
        if (!isNotificationChannelBlocked(this, Constants.SUPPORT_CHANNEL_ID)) {
            showSupportAppNotification(this, GaCategory.SERVICE)
            historyKeeper?.inAppPurchasePromptShown()
            return
        }

        AppEvents.notificationBlocked(GaLabels.NOTIFICATION_SUPPORT_APP).track()
    }

    private fun showRateAppPrompt() {
        if (!isNotificationChannelBlocked(this, Constants.SUPPORT_CHANNEL_ID)) {
            showReviewAppNotification(this, GaCategory.SERVICE)
            historyKeeper?.rateAppPromptShown()
            return
        }

        AppEvents.notificationBlocked(GaLabels.NOTIFICATION_RATE_APP).track()
    }

    private fun showRating(rating: MovieRating) {
        displayer?.showRatingWindow(rating)
        if (preferences?.isSettingEnabled(UserPreferences.TTS_AVAILABLE) == true && preferences?.isSettingEnabled(UserPreferences.USE_TTS) == true) {
            AppEvents.SPEAK_RATING.track()
            speaker?.talk(rating)
        }
    }
}