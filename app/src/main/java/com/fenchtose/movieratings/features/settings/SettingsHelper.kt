package com.fenchtose.movieratings.features.settings

import android.support.annotation.IdRes
import android.support.v7.widget.SwitchCompat
import android.view.View
import com.fenchtose.movieratings.model.preferences.UserPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsHelper(
    private val preferences: UserPreferences,
    private val root: View,
    onUpdate: (key: String) -> Unit
) {

    private val publisher = PublishSubject.create<String>()
    private val disposable: Disposable

    init {
        disposable = publisher
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onUpdate(it)
            }
    }

    fun addAppToggle(@IdRes buttonId: Int, key: String) {
        val toggle = root.findViewById<SwitchCompat?>(buttonId)
        toggle?.let {
            it.visibility = View.VISIBLE
            it.isChecked = preferences.isAppEnabled(key)
            it.setOnCheckedChangeListener { _, isChecked ->
                updatePreference(preferences, key, isChecked)
            }
        }
    }

    fun addSettingToggle(@IdRes buttonId: Int, key: String) {
        val toggle = root.findViewById<SwitchCompat?>(buttonId)
        toggle?.let {
            it.visibility = View.VISIBLE
            it.isChecked = preferences.isSettingEnabled(key)
            it.setOnCheckedChangeListener { _, isChecked ->
                updatePreference(preferences, key, isChecked)
            }
        }
    }

    private fun updatePreference(preferences: UserPreferences, app: String, checked: Boolean) {
        preferences.setEnabled(app, checked)
        publisher.onNext(app)
    }

    fun clear() {
        disposable.dispose()
        publisher.onComplete()
    }
}