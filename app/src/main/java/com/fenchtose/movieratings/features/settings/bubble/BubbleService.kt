package com.fenchtose.movieratings.features.settings.bubble

import android.app.Service
import android.content.Intent
import com.fenchtose.movieratings.base.router.EventBus
import com.fenchtose.movieratings.display.RatingDisplayer
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import io.reactivex.disposables.CompositeDisposable

class BubbleService : Service() {

    private var displayer: RatingDisplayer? = null

    private var disposables: CompositeDisposable? = null

    override fun onCreate() {
        super.onCreate()
        disposables = CompositeDisposable()
        val preferences = SettingsPreferences(this)
        displayer = RatingDisplayer(this, preferences, false)
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val rating = MovieRating("test", 8.2f, 0, "Movie title", "", "", "", 2018, -1)
        displayer?.showRatingWindow(rating)

        disposables?.add(
            EventBus.subscribe<BubbleColorEvent>()
                .subscribe { event ->
                    displayer?.let {
                        it.updateColor(event.color)
                        if (!it.isShowingView) {
                            it.showRatingWindow(rating)
                        }
                    }
                }
        )

        disposables?.add(
            EventBus.subscribe<BubbleDetailEvent>().subscribe { event ->
                displayer?.let {
                    it.updateSize(event.size)
                    if (!it.isShowingView) {
                        it.showRatingWindow(rating)
                    }
                }
            }
        )

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribe()
        displayer?.removeView()
    }

    private fun unsubscribe() {
        disposables?.dispose()
    }
}