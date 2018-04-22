package com.fenchtose.movieratings.display

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.features.stickyview.FloatingRatingView
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.IntentUtils
import com.fenchtose.movieratings.util.ToastUtils
import java.lang.ref.WeakReference

class RatingDisplayer(ctx: Context, val analytics: AnalyticsDispatcher, private val preferences: UserPreferences) {
    private val context: Context = ctx.applicationContext

    private val TAG = "RatingDisplayer"

    var isShowingView: Boolean = false
    private var ratingView: WeakReference<FloatingRatingView?> = WeakReference(null)

    private val handler = Handler(Looper.getMainLooper())

    fun showRatingWindow(movie: Movie) {
        if (movie.ratings.isEmpty()) {
            return
        }

        if (!AccessibilityUtils.canDrawOverWindow(context)) {
            Log.e(TAG, "no drawing permission")
            val duration = preferences.getToastDuration()
            ToastUtils.showMovieRating(context, movie, duration)
            return
        }

        if (ratingView.get() == null) {
            ratingView = WeakReference(FloatingRatingView(context))
        }

        ratingView.get()?.let {
            it.movie = movie

            if (it.parent != null) {
                return
            }

            isShowingView = if (addViewToWindow(it)) {
                true
            } else {
                ratingView = WeakReference(null)
                false
            }
        }

    }

    private fun addViewToWindow(view: FloatingRatingView): Boolean {
        val manager = getWindowManager()
        val dm = DisplayMetrics()
        manager.defaultDisplay.getMetrics(dm)

        val width = (136*dm.density).toInt()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT ,
                dm.widthPixels-width, (180*dm.density).toInt(),
                layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        return try {
            manager.addView(view, params)
            view.setOnTouchListener(floatingWindowTouchListener)
            true
        } catch (e: RuntimeException) {
            e.printStackTrace()
            analytics.sendEvent(Event("runtime_error")
                    .putAttribute("error", if (e.message != null) e.message!! else "unknown")
                    .putAttribute("where", "service_remove_view"))
            false
        }
    }

    fun removeView() {
        isShowingView = false
        handler.postDelayed({
            val view = ratingView.get()
            view?.let {
                view.parent?.let {
                    try {
                        getWindowManager().removeViewImmediate(view)
                    } catch(e: RuntimeException) {
                        e.printStackTrace()
                        analytics.sendEvent(Event("runtime_error")
                                .putAttribute("error", if (e.message != null) e.message!! else "unknown")
                                .putAttribute("where", "service_remove_view"))
                    }
                }
            }
        }, 10)
    }

    private fun getWindowManager() : WindowManager {
        return context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val floatingWindowTouchListener = View.OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.x < v.context.resources.displayMetrics.density * 40) {
                removeView()
                analytics.sendEvent(Event("fw_close_clicked"))
                return@OnTouchListener true
            } else {
                analytics.sendEvent(Event("fw_open_clicked"))
                IntentUtils.openImdb(context, (v as FloatingRatingView).movie?.imdbId)
            }
        }

        false
    }
}