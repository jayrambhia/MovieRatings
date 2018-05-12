package com.fenchtose.movieratings.features.settings

import android.support.design.widget.Snackbar
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class PreferenceUpdater(val root: ViewGroup) {
    private val publisher: PublishSubject<String> = PublishSubject.create()
    private val disposable: Disposable

    init {
        disposable = publisher
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showUpdatePreferenceSnackbar() }
    }

    private fun showUpdatePreferenceSnackbar() {
        Snackbar.make(root, R.string.settings_preference_update_content, Snackbar.LENGTH_SHORT).show()
    }

    fun show(app: String) {
        publisher.onNext(app)
    }

    fun release() {
        publisher.onComplete()
        disposable.dispose()
    }
}