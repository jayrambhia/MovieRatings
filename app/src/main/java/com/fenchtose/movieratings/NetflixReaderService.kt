package com.fenchtose.movieratings

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.display.RatingDisplayer
import com.fenchtose.movieratings.features.tts.Speaker
import com.fenchtose.movieratings.model.api.provider.MovieRatingsProvider
import com.fenchtose.movieratings.model.api.provider.RatingRequest
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.inAppAnalytics.DbHistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.HistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.PreferenceUserHistory
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit


class NetflixReaderService : AccessibilityService() {

    private var title: String? = null
    private val TAG: String = "NetflixReaderService"

    private var provider: MovieRatingsProvider? = null

    private var preferences: UserPreferences? = null

    // For Samsung S6 edge, we are getting TYPE_WINDOW_STATE_CHANGED for adding floating window which triggers removeView()
//    private val supportedPackages: Array<String> = arrayOf(Constants.PACKAGE_NETFLIX, Constants.PACKAGE_PRIMEVIDEO, Constants.PACKAGE_PLAY_MOVIES_TV, Constants.PACKAGE_HOTSTAR, Constants.PACKAGE_BBC_IPLAYER, Constants.PACKAGE_JIO_TV/*, Constants.PACKAGE_YOUTUBE*//*, BuildConfig.APPLICATION_ID*/)
    private val supportedPackages = Constants.supportedApps.keys

    private var lastWindowStateChangeEventTime: Long = 0
    private val WINDOW_STATE_CHANGE_THRESHOLD = 2000

    private var analytics: AnalyticsDispatcher? = null

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

        preferences = SettingsPreferences(this)
        provider = MovieRatingsApplication.ratingProviderModule.ratingProvider
        analytics = MovieRatingsApplication.analyticsDispatcher
        displayer = RatingDisplayer(this, analytics!!, preferences!!)
        historyKeeper = DbHistoryKeeper(PreferenceUserHistory(MovieRatingsApplication.instance!!),
                DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao()),
                preferences!!)

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

            if (resourceRemover == null) {
                resourceRemover = PublishSubject.create()
                resourceRemover
                        ?.debounce(RESOURCE_THRESHOLD, TimeUnit.SECONDS)
                        ?.subscribe({
                            clearResources()
                        })
            }

            resourceRemover?.onNext(true)
        }
    }

    private fun clearResources() {
        synchronized(this) {
            speaker?.shutdown()
            speaker = null
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

        val info = event.source
        info?.let {

            val isAppEnabled = when (it.packageName) {
                BuildConfig.APPLICATION_ID -> true
                /*Constants.PACKAGE_NETFLIX -> preferences?.isAppEnabled(UserPreferences.NETFLIX)
                Constants.PACKAGE_PRIMEVIDEO -> preferences?.isAppEnabled(UserPreferences.PRIMEVIDEO)
                Constants.PACKAGE_PLAY_MOVIES_TV -> preferences?.isAppEnabled(UserPreferences.PLAY_MOVIES)
                Constants.PACKAGE_HOTSTAR -> preferences?.isAppEnabled(UserPreferences.HOTSTAR)
                Constants.PACKAGE_YOUTUBE -> preferences?.isAppEnabled(UserPreferences.YOUTUBE)
                Constants.PACKAGE_BBC_IPLAYER -> preferences?.isAppEnabled(UserPreferences.BBC_IPLAYER)*/
                else -> preferences?.isAppEnabled(it.packageName.toString())
            }

            if (isAppEnabled == null || !isAppEnabled) {
                return@let
            }

            val titles: List<CharSequence> = when(it.packageName) {
                BuildConfig.APPLICATION_ID -> it.findAccessibilityNodeInfosByViewId(BuildConfig.APPLICATION_ID + ":id/flutter_test_title").filter { it.text != null }.map { it.text }
                Constants.PACKAGE_NETFLIX -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_title")
                        .filter { it.text != null }
                        .map {
                            val text = it.text
                            it.recycle()
                            text
                        }
                Constants.PACKAGE_PRIMEVIDEO -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PRIMEVIDEO + ":id/TitleText")
                        .filter { it.text != null }
                        .map {
                            val text = it.text
                            it.recycle()
                            text
                        }
                Constants.PACKAGE_PLAY_MOVIES_TV ->  {
                    val nodes = ArrayList<CharSequence>()
                    if (event.className == "com.google.android.apps.play.movies.mobile.usecase.details.DetailsActivity" && event.text != null) {
                        val text = event.text.toString().replace("[", "").replace("]", "")
                        nodes.add(text)
                    }
                    nodes
                }
                Constants.PACKAGE_HOTSTAR -> {
                    val nodes = ArrayList<CharSequence>()
                    // it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_HOTSTAR + ":id/metadata_title").filter { it.text != null }.map { it.text }
                    if (event.className == "in.startv.hotstar.rocky.detailpage.HSDetailPageActivity" && event.text != null) {
                        val text = event.text.toString().replace("[", "").replace("]", "")
                        nodes.add(text)
                    }
                    nodes
                }
                Constants.PACKAGE_YOUTUBE -> {
//                    checkNodeRecursively(it, 0)
                    val nodes = ArrayList<CharSequence>()
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_YOUTUBE + ":id/watch_list").filter { it.childCount > 0 }
                            .flatMap {
                                findChildrenWithId(it, Constants.PACKAGE_YOUTUBE + ":id/title")
                                        .filter { it.text != null && it.parent?.viewIdResourceName == null}
                                        .map {
                                            val text = it.text
                                            it.recycle()
                                            text
                                        }
                            }.toCollection(nodes)
                    nodes
                }
                Constants.PACKAGE_BBC_IPLAYER -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_BBC_IPLAYER + ":id/programme_details_title")
                            .filter { it.text != null }
                            .map {
                                val text = it.text
                                it.recycle()
                                text
                            }
                }
                Constants.PACKAGE_JIO_TV -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_JIO_TV + ":id/program_name")
                            .filter { it.text != null }
                            .map {
                                val text = it.text
                                it.recycle()
                                text
                            }
                }
                Constants.PACKAGE_JIO_CINEMA -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_JIO_CINEMA + ":id/tvShowName")
                            .filter { it.text != null }
                            .map {
                                val text = it.text
                                it.recycle()
                                text
                            }
                }
                else -> {
                    checkNodeRecursively(it, 0)
                    ArrayList()
                }
            }.distinctBy { it }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "scraped titles: $titles")
            }

            val years: List<CharSequence> = when(it.packageName) {
                Constants.PACKAGE_NETFLIX -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_basic_info_year")
                        .filter { it.text != null }
                        .map {
                            val text = it.text
                            it.recycle()
                            text
                        }
                Constants.PACKAGE_PRIMEVIDEO -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PRIMEVIDEO + ":id/ItemMetadataView")
                            // get children of that node
                            .flatMap {
                                val children = ArrayList<CharSequence>()
                                (0 until it.childCount).map {
                                    i ->
                                        var text: CharSequence
                                        try {
                                            val child = it.getChild(i)
                                            text = child.text
                                            child.recycle()
                                        } catch (e: IllegalStateException) {
                                            e.printStackTrace()
                                            text = ""
                                        }
                                        text
                                }.filter {
                                    it != null && it.isNotBlank()
                                }
                                .toCollection(children)
                                children
                            }
                            // filter node which has text containing 4 digits
                            .filter {
                                !FixTitleUtils.fixPrimeVideoYear(it.toString()).isNullOrEmpty()
                            }
                }
                Constants.PACKAGE_PLAY_MOVIES_TV ->  {
                    val nodes = ArrayList<CharSequence>()
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PLAY_MOVIES_TV + ":id/play_header_listview")
                            .takeIf { it.size > 0 }
                            ?.firstOrNull()
                            ?.run {
                                (0 until childCount).map {
                                    i -> getChild(i)
                                }.filter {
                                    it != null && it.text != null && it.className.contains("TextView") && FixTitleUtils.matchesPlayMoviesYear(it.text.toString())
                                }.map {
                                    val text = it.text
                                    it.recycle()
                                    text
                                }
                                 .toCollection(nodes)
                            }

                    nodes
                }
                Constants.PACKAGE_HOTSTAR -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_HOTSTAR + ":id/metadata_subtitle")
                            .filter { it.text != null }
                            .map {
                                val text = it.text
                                it.recycle()
                                text
                            }
                            .filter {
                                !FixTitleUtils.fixHotstarYear(it.toString()).isNullOrEmpty()
                            }
                }
                Constants.PACKAGE_JIO_CINEMA -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_JIO_CINEMA + ":id/tvMovieSubtitle")
                            .filter { it.text != null }
                            .map {
                                val text = it.text
                                it.recycle()
                                text
                            }
                            .filter {
                                !FixTitleUtils.fixJioCinemaYear(it.toString()).isNullOrEmpty()
                            }
                }
                else -> ArrayList()
            }.distinctBy { it }

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

        }

//        event.recycle()
    }

    @Suppress("unused")
    private fun checkNodeRecursively(info: AccessibilityNodeInfo?, level: Int) {
        if (!BuildConfig.DEBUG) {
            return
        }

        info?.let {

            Log.d(TAG, "${" ".repeat(level)}info: text: ${it.text}, id: ${it.viewIdResourceName}, class: ${it.className}, parent id: ${it.parent?.viewIdResourceName}, desc=${it.contentDescription?.toString()}")
            /*it.contentDescription?.let {
//                Log.d(TAG, "type: ${it.javaClass}")
//                Log.d(TAG, "sub: ${it.subSequence(0, 4)} + ${it.subSequence(4, it.length)}, length: ${it.length}")
                if (it is Spanned) {
                    val s = it as Spanned
                    Log.d(TAG, "Spans: ${s.getSpans<Any>()}")
                }
            }*/
            if (info.childCount > 0) {
                Log.d(TAG, "${" ".repeat(level)}--- <children> ---")
                (0 until info.childCount)
                        .forEach { index ->
                            checkNodeRecursively(it.getChild(index), level + 1)
                        }

                Log.d(TAG, "${" ".repeat(level)}--- </children> ---")
            }
        }
    }

    private fun findChildrenWithId(node: AccessibilityNodeInfo, id: String): ArrayList<AccessibilityNodeInfo> {
        val children = ArrayList<AccessibilityNodeInfo>()
        recursiveFindChildrenWithId(children, node, id)
        return children
    }

    private fun recursiveFindChildrenWithId(result: ArrayList<AccessibilityNodeInfo>, node: AccessibilityNodeInfo, id: String) {
        var added = false
        if (node.viewIdResourceName == id) {
            result.add(node)
            added = true
        }

        if (node.childCount > 0) {
            (0 until node.childCount)
                    .forEach {
                        index ->
                        val child = node.getChild(index)
                        recursiveFindChildrenWithId(result, child, id)
                    }
        }

        if (!added) {
            node.recycle()
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
                    year = if (preferences?.isAppEnabled(UserPreferences.USE_YEAR) == true) year else null)

            requestPublisher.onNext(request)
        }
    }

    private fun fixTitle(@Suppress("UNUSED_PARAMETER") packageName: CharSequence, text: String): String {
        return FixTitleUtils.clean(text)
    }

    private fun fixYear(packageName: CharSequence, text: String?): String {
        text?.let {
            val fixed = when(packageName) {
                Constants.PACKAGE_NETFLIX -> FixTitleUtils.fixNetflixYear(it)
                Constants.PACKAGE_PRIMEVIDEO -> FixTitleUtils.fixPrimeVideoYear(it)
                Constants.PACKAGE_PLAY_MOVIES_TV -> FixTitleUtils.fixPlayMoviesYear(it)
                Constants.PACKAGE_HOTSTAR -> FixTitleUtils.fixHotstarYear(it)
                Constants.PACKAGE_JIO_CINEMA -> FixTitleUtils.fixJioCinemaYear(it)
                else -> ""
            }

            fixed?.let {
                return it
            }
        }

        return ""
    }

    private fun getMovieInfo(request: RatingRequest) {
        initResources()

        GaEvents.GET_RATINGS.withLabelArg(request.appName).track()

        provider?.let {
            it.useFlutterApi(preferences?.isAppEnabled(UserPreferences.USE_FLUTTER_API) != false)
            it.getMovieRating(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(myScheduler)
                    .subscribe({
                        if (it.imdbId.isNotEmpty()) {
                            showRating(it)
                            historyPublisher.onNext(request)
                        }
                    }, {
                        if (it is HttpException) {
                            if (it.code() == 404) {
                                update404(request.title, request.year)
                            }
                        }
                        it.printStackTrace()
                    })
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
    }

    private fun checkForSupportPrompt() {
        historyKeeper?.let {
            it.shouldShowSupportAppPrompt()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        if (it) {
                            showSupportAppPrompt()
                        } else {
                            checkForRateAppPrompt()
                        }
                    })
        }
    }

    private fun checkForRateAppPrompt() {
        historyKeeper?.let {
            it.shouldShowRateAppPrompt()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({
                        if (it) {
                            showRateAppPrompt()
                        }
                    })
        }
    }

    private fun showSupportAppPrompt() {
        if (!isNotificationChannelBlocked(this, Constants.SUPPORT_CHANNEL_ID)) {
            showSupportAppNotification(this)
            historyKeeper?.inAppPurchasePromptShown()
        }
    }

    private fun showRateAppPrompt() {
        if (!isNotificationChannelBlocked(this, Constants.SUPPORT_CHANNEL_ID)) {
            showReviewAppNotification(this)
            historyKeeper?.rateAppPromptShown()
        }
    }

    private fun showRating(rating: MovieRating) {
        displayer?.showRatingWindow(rating)
        if (preferences?.isSettingEnabled(UserPreferences.TTS_AVAILABLE) == true && preferences?.isSettingEnabled(UserPreferences.USE_TTS) == true) {
            speaker?.talk(rating)
        }
    }
}