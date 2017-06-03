package com.fenchtose.movieratings

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.support.v4.view.accessibility.AccessibilityEventCompat
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.api.provider.RetrofitMovieProvider
import com.fenchtose.movieratings.features.sticky_view.FloatingRatingView
import com.fenchtose.movieratings.util.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.preferences.SettingsPreference


class NetflixReaderService : AccessibilityService() {

    private var title: String? = null
    private val TAG: String = "NetflixReaderService"

    private var provider: MovieProvider? = null

    private var handler: Handler? = null
    private var ratingView: WeakReference<FloatingRatingView?> = WeakReference(null)

    private var preferences: SettingsPreference? = null

    private val supportedPackages: Array<String> = arrayOf("com.netflix.mediaclient")

    private var lastWindowStateChangeEventTime: Long = 0
    private val WINDOW_STATE_CHANGE_THRESHOLD = 2000
    private var isShowingView: Boolean = false

    private var analytics: AnalyticsDispatcher? = null

    override fun onCreate() {
        super.onCreate()

        preferences = SettingsPreference(this)

        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.OMDB_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        val dao = MovieRatingsApplication.getDatabase().movieDao()

        provider = RetrofitMovieProvider(retrofit, dao)

        handler = Handler()

        analytics = MovieRatingsApplication.getAnalyticsDispatcher()

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        Log.d(TAG, "eventt: " + AccessibilityEvent.eventTypeToString(event.eventType) + ", " + event.packageName)

        if (!supportedPackages.contains(event.packageName)) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && isShowingView) {
                if (System.currentTimeMillis() - lastWindowStateChangeEventTime > WINDOW_STATE_CHANGE_THRESHOLD) {
                    // User has moved to some other app
                    removeView()
                    title = null
                }
            }

            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            removeView()
            lastWindowStateChangeEventTime = System.currentTimeMillis()
            title = null
        }

        preferences?.let {
            if (!preferences!!.isAppEnabled(SettingsPreference.NETFLIX)) {
                return
            }
        }

        val record = AccessibilityEventCompat.asRecord(event)
        val info = record.source
        info?.let {
            val titles = info.findAccessibilityNodeInfosByViewId("com.netflix.mediaclient:id/video_details_title")
            if (titles != null && titles.size > 0) {
                titles.filter { it.text != null }
                        .forEach {
                            setMovieTitle(it.text.toString())
                        }
            }
        }
    }


    override fun onInterrupt() {
        Log.d(TAG, "on interrupt")
    }


    private fun setMovieTitle(text: String) {
        if (title == null || title != text) {
            title = text
            Log.i(TAG, "Movie Title: " + title!!)
            getMovieInfo(text)
        }
    }

    private fun getMovieInfo(title: String) {

        analytics?.sendEvent(Event("get_movie").putAttribute("title", title))

        provider?.let {
            provider!!.getMovie(title)
                    .subscribeOn(Schedulers.io())
                    .filter { it.ratings.size > 0 }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onNext = {
                        Log.d(TAG, "got movie info: " + it.title)
                        showRating(it)
                    }, onError = {
                        it.printStackTrace()
                    })
        }
    }

    private fun showRating(movie: Movie) {
        showRatingWindow(movie.title, movie.ratings[0].value)
    }

    @Suppress("unused")
    private fun showNotification(movie: String, rating: String) {
        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Movie: " + movie)
                .setContentText("Rating: " + rating)
                .setAutoCancel(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.DEFAULT_VIBRATE

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun showRatingWindow(@Suppress("UNUSED_PARAMETER") movie: String, rating: String) {
        if (ratingView.get() == null) {
            val view = FloatingRatingView(this)
            ratingView = WeakReference(view)
        }

        val view = ratingView.get()

        view?.let {
            view.setRating(rating)

            val parent = view.parent
            parent?.let {
                return
            }

            addViewToWindow(view)
            isShowingView = true
        }

    }

    private fun addViewToWindow(view: FloatingRatingView) {
        val manager = getWindowManager()
        val dm = DisplayMetrics()
        manager.defaultDisplay.getMetrics(dm)

        val width = (136*dm.density).toInt()

        val params = WindowManager.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT ,
                dm.widthPixels-width, (180*dm.density).toInt(),
                WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        manager.addView(view, params)
        view.setOnTouchListener(floatingWindowTouchListener)
    }

    private fun removeView() {
        isShowingView = false
        handler?.postDelayed({
            val view = ratingView.get()
            view?.let {
                view.parent?.let {
                    getWindowManager().removeViewImmediate(view)
                }
            }
        }, 10)
    }

    private fun getWindowManager() : WindowManager {
        return getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val floatingWindowTouchListener = View.OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.x < v.context.resources.displayMetrics.density * 30) {
                removeView()
                analytics?.sendEvent(Event("fw_close_clicked"))
                return@OnTouchListener true
            }
        }

        false
    }
}
