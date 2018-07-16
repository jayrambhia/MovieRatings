package com.fenchtose.movieratings.features.settings.bubble

import android.app.Service
import android.content.Intent
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.router.EventBus
import com.fenchtose.movieratings.display.RatingDisplayer
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.entity.Rating
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import io.reactivex.disposables.Disposable

class BubbleService: Service() {

    private var displayer: RatingDisplayer? = null

    private var disposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        val preferences = SettingsPreferences(this)
        val analytics = MovieRatingsApplication.analyticsDispatcher
        displayer = RatingDisplayer(this, analytics, preferences, false)
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rating = MovieRating()
        rating.imdbId = "test"
        rating.rating = 8.2f
        displayer?.showRatingWindow(rating)

        disposable = EventBus.subscribe<BubbleColorEvent>()
                .subscribe({
                    event ->
                    displayer?.let {
                        it.updateColor(event.color)
                        if (!it.isShowingView) {
                            displayer?.showRatingWindow(rating)
                        }
                    }
                }, { })

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribe()
        displayer?.removeView()
    }

    private fun unsubscribe() {
        disposable?.dispose()
    }
}