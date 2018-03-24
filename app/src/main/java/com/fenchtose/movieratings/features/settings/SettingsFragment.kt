package com.fenchtose.movieratings.features.settings

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AccessibilityUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsFragment: BaseFragment() {

    private var root: ViewGroup? = null
    private var updatePublisher: PublishSubject<Boolean>? = null

    private var preferences: UserPreferences? = null

    private var toastDuration: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.settings_page_layout, container, false) as ViewGroup
        return root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = SettingsPreferences(context)

        this.preferences = preferences

        addAppToggle(preferences, view, R.id.netflix_toggle, UserPreferences.NETFLIX)
        addAppToggle(preferences, view, R.id.prime_video_toggle, UserPreferences.PRIMEVIDEO)
        addAppToggle(preferences, view, R.id.save_browsing_toggle, UserPreferences.SAVE_HISTORY)

        val toastInfo = view.findViewById<TextView>(R.id.toast_duration_info)
        val toastSeekbar = view.findViewById<SeekBar>(R.id.toast_duration_seekbar)
        val seekbarContainer = view.findViewById<View>(R.id.seekbar_container)
        toastDuration = view.findViewById(R.id.toast_duration_view)

        val showToastDurationInfo = !AccessibilityUtils.canDrawOverWindow(context)
        if (!showToastDurationInfo) {
            toastInfo?.visibility = GONE
            toastSeekbar?.visibility = GONE
            toastDuration?.visibility = GONE
            seekbarContainer?.visibility = GONE
        } else {
            val progress = preferences.getToastDuration()/1000
            toastDuration?.text = (progress).toString()
            toastSeekbar?.progress = progress

            toastSeekbar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateToastDuration((progress + 1) * 1000)
                }
            })
        }

        val publisher = PublishSubject.create<Boolean>()
        subscribe(publisher
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showUpdatePreferenceSnackbar() })

        updatePublisher = publisher
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.onComplete()
    }

    private fun addAppToggle(preferences: UserPreferences, root: View, @IdRes buttonId: Int, key: String) {
        val toggle = root.findViewById<SwitchCompat?>(buttonId)
        toggle?.let {
            it.visibility = View.VISIBLE
            it.isChecked = preferences.isAppEnabled(key)
            it.setOnCheckedChangeListener {
                _, isChecked -> updatePreference(preferences, key, isChecked)
            }
        }
    }

    private fun updatePreference(preferences: UserPreferences, app: String, checked: Boolean) {
        preferences.setAppEnabled(app, checked)
        updatePublisher?.onNext(true)
    }

    private fun updateToastDuration(durationInMs: Int) {
        preferences?.setToastDuration(durationInMs)
        updatePublisher?.onNext(true)
        toastDuration?.text = (preferences!!.getToastDuration()/1000).toString()
    }

    private fun showUpdatePreferenceSnackbar() {
        root?.let {
            Snackbar.make(it, R.string.settings_preference_update_content, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.settings_header
    }

    class SettingsPath: RouterPath<SettingsFragment>() {

        override fun createFragmentInstance(): SettingsFragment {
            return SettingsFragment()
        }

    }

}