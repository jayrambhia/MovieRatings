package com.fenchtose.movieratings.features.settings

import android.os.Bundle
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
import com.fenchtose.movieratings.model.preferences.SettingsPreference
import com.fenchtose.movieratings.util.AccessibilityUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsFragment: BaseFragment() {

    private var root: ViewGroup? = null
    private var updatePublisher: PublishSubject<Boolean>? = null

    private var preferences: SettingsPreference? = null

    private var netflixToggle: SwitchCompat? = null

    private var toastInfo: View? = null
    private var toastSeekbar: SeekBar? = null
    private var toastDuration: TextView? = null

//    private val TAG = "SettingsFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.settings_page_layout, container, false) as ViewGroup
        return root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = SettingsPreference(context)

        netflixToggle = view.findViewById(R.id.netflix_toggle) as SwitchCompat
        netflixToggle?.let {
            preferences?.let {
                val netflix = preferences!!.isAppEnabled(SettingsPreference.NETFLIX)
                netflixToggle!!.isChecked = netflix
                netflixToggle!!.setOnCheckedChangeListener {
                    view, isChecked ->  updatePreference(SettingsPreference.NETFLIX, isChecked)
                }
            }
        }

        toastInfo = view.findViewById(R.id.toast_duration_info)
        toastSeekbar = view.findViewById(R.id.toast_duration_seekbar) as SeekBar
        toastDuration = view.findViewById(R.id.toast_duration_view) as TextView

        val showToastDurationInfo = !AccessibilityUtils.isDrawOverWindowSupported(context)
        if (!showToastDurationInfo) {
            toastInfo?.visibility = GONE
            toastSeekbar?.visibility = GONE
            toastDuration?.visibility = GONE
        } else {
            toastSeekbar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateToastDuration((progress + 1) * 1000)
                }
            })

            if (preferences != null) {
                val progress = preferences!!.getToastDuration()/1000
                toastDuration?.text = (progress).toString()
                toastSeekbar?.progress = progress
            }
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

    private fun updatePreference(app: String, checked: Boolean) {
        preferences?.setAppEnabled(app, checked)
        updatePublisher?.onNext(true)
    }

    private fun updateToastDuration(durationInMs: Int) {
        preferences?.setToastDuration(durationInMs)
        updatePublisher?.onNext(true)
        toastDuration?.text = (preferences!!.getToastDuration()/1000).toString()
    }

    private fun showUpdatePreferenceSnackbar() {
        root?.let {
            Snackbar.make(root!!, R.string.settings_preference_update_content, Snackbar.LENGTH_SHORT).show()
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