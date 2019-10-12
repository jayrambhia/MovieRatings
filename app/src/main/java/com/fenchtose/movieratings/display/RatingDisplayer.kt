package com.fenchtose.movieratings.display

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.analytics.ga.GaLabels
import com.fenchtose.movieratings.features.stickyview.BubbleSize
import com.fenchtose.movieratings.features.stickyview.FloatingRating
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AccessibilityUtils
import com.fenchtose.movieratings.util.IntentUtils
import com.fenchtose.movieratings.util.ToastUtils
import com.fenchtose.movieratings.widgets.RatingBubble

class RatingDisplayer(ctx: Context,
                      private val preferences: UserPreferences,
                      private val autoDismiss: Boolean = true,
                      private val analytics: Boolean = false) {
    private val context: Context = ctx.applicationContext

    private var touchListener: BubbleTouchListener? = null

    private val TAG = "RatingDisplayer"

    var isShowingView: Boolean = false
    private var floatingRating: FloatingRating? = null
    private val handler = Handler(Looper.getMainLooper())
    private val width = context.resources.getDimensionPixelOffset(R.dimen.floating_rating_view_width)

    private val dismissRunnable = Runnable {
        removeView()
    }

    private val removeRunnable = Runnable {
        floatingRating?.let {
            removeViewImmediate(it.getBubbleView())
        }
    }

    fun showRatingWindow(rating: MovieRating) {
        if (rating.imdbId.isEmpty() || (rating.rating <= 0f && !rating.is404())) {
            removeView()
            return
        }

        val bubbleColor = preferences.getBubbleColor(ContextCompat.getColor(context, R.color.floating_rating_color))
        val bubbleSize = if (preferences.isAppEnabled(UserPreferences.RATING_DETAILS) || rating.is404()) BubbleSize.BIG else BubbleSize.SMALL

        if (!AccessibilityUtils.canDrawOverWindow(context)) {
            if (rating.is404()) {
                // No point in showing a toast.
                return
            }

            Log.e(TAG, "no drawing permission")
            ToastUtils.showMovieRating(context, rating, bubbleColor)
            trackEvent(GaEvents.SHOW_RATINGS.withLabel(GaLabels.TOAST))
            return
        }

        if (floatingRating == null) {
            floatingRating = FloatingRating(context, bubbleColor, bubbleSize)
        }

        floatingRating?.let {
            it.rating = rating
            it.updateColor(bubbleColor)
            it.updateSize(bubbleSize)
            resetAutoDismissRunners()

            val label = when {
                rating.is404() -> GaLabels.RATING_404
                else -> when(bubbleSize) {
                    BubbleSize.SMALL -> GaLabels.BUBBLE_SMALL
                    BubbleSize.BIG -> GaLabels.BUBBLE_BIG
                }
            }

            trackEvent(GaEvents.SHOW_RATINGS.withLabel(label))
            if (it.getBubbleView().parent != null) {
                return
            }

            isShowingView = if (addViewToWindow(it.getBubbleView())) {
                true
            } else {
                floatingRating = null
                false
            }
        }

    }

    fun show404(title: String) {
        if (title.isBlank()) {
            removeView()
            return
        }

        showRatingWindow(MovieRating.create404Dummy(title))
    }

    @SuppressLint("RtlHardcoded")
    private fun addViewToWindow(bubble: RatingBubble): Boolean {
        val manager = getWindowManager()
        val dm = DisplayMetrics()
        manager.defaultDisplay.getMetrics(dm)

        val position = preferences.getBubblePosition(dm.heightPixels - context.resources.getDimensionPixelOffset(R.dimen.floating_rating_view_y))

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(width, WindowManager.LayoutParams.WRAP_CONTENT ,
                if (position.second) 0 else dm.widthPixels-width, position.first,
                layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        params.gravity = Gravity.TOP or Gravity.LEFT

        bubble.updateDirection(position.second)

        if (touchListener == null) {
            touchListener = BubbleTouchListener(manager, context, dm.widthPixels-width,
                    bubbleCallback)
        }

        return try {
            manager.addView(bubble, params)
            touchListener?.attach(bubble)
            true
        } catch (e: RuntimeException) {
            e.printStackTrace()
            false
        }
    }

    fun updateColor(@ColorInt color: Int) {
        floatingRating?.updateColor(color)
    }

    fun updateSize(size: BubbleSize) {
        floatingRating?.updateSize(size)
    }

    fun removeView() {
        isShowingView = false
        handler.postDelayed(removeRunnable, 10)
    }

    private fun resetAutoDismissRunners() {
        handler.removeCallbacks(dismissRunnable)
        handler.removeCallbacks(removeRunnable)

        val duration = preferences.getRatingDisplayDuration()
        if (duration > 0 && autoDismiss) {
            handler.postDelayed(dismissRunnable, duration.toLong())
        }
    }

    private fun removeViewImmediate(view: View) {
        try {
            Log.d(TAG, "remove view immediate: $view")
            getWindowManager().removeViewImmediate(view)
        } catch(e: RuntimeException) {
            e.printStackTrace()
        } finally {
            isShowingView = false
            floatingRating = null
        }

        touchListener?.release()
    }

    private fun getWindowManager() : WindowManager {
        return context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val bubbleCallback = object: BubbleTouchListener.BubbleCallback {
        override fun onClick(bubble: RatingBubble?, x: Int, y: Int) {
            bubble?.let {
                if (it.isClickForClose(x)) {
                    trackEvent(GaEvents.DISMISS_RATING)
                    removeViewImmediate(it)
                } else {
                    openBubble()
                }
            }
        }

        override fun updatePosition(bubble: RatingBubble?, y: Int, left: Boolean) {
            preferences.setBubblePosition(y, left)
            resetAutoDismissRunners()
        }

    }

    private fun openBubble() {
        val rating = floatingRating?.rating ?: return
        val opened = when {
            rating.is404() -> openImdbSearch(rating.title)
            else -> openTitle(rating)
        }

        if (opened) {
            removeView()
        }
    }

    private fun openImdbSearch(title: String): Boolean {
        return IntentUtils.openImdbSearch(context, title)
    }

    private fun openTitle(rating: MovieRating): Boolean {
        val openInApp = preferences.isAppEnabled(UserPreferences.OPEN_MOVIE_IN_APP)
        val label = when(rating.source) {
            "MAL" -> "mal"
            "IMDB" -> if (openInApp) "app" else "imdb"
            else -> null
        }

        label?.let { trackEvent(GaEvents.RATING_OPEN_MOVIE.withLabel(it)) }
        return IntentUtils.openMovie(context, rating, openInApp)
    }

    private fun trackEvent(event: Event) {
        if (analytics) {
            event.track()
        }
    }
}