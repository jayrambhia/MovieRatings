package com.fenchtose.movieratings.features.settings

import android.app.AlertDialog
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
import com.fenchtose.movieratings.MainActivity
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AccessibilityUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsFragment: BaseFragment() {

    private var root: ViewGroup? = null
    private var updatePublisher: PublishSubject<String>? = null

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
        addAppToggle(preferences, view, R.id.show_activate_toggle, UserPreferences.SHOW_ACTIVATE_FLUTTER)

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
            toastSeekbar?.progress = progress - 1

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

        view.findViewById<View>(R.id.clear_history_button).setOnClickListener { showClearHistoryDialog() }
        view.findViewById<View>(R.id.delete_data_button).setOnClickListener { showDeleteDataDialog() }


        val publisher = PublishSubject.create<String>()
        subscribe(publisher
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    when(it) {
                        UserPreferences.SHOW_ACTIVATE_FLUTTER -> {
                            activity?.let {
                                if (it is MainActivity) {
                                    it.triggerAccessibilityCheck()
                                }
                            }
                        }
                    }
                }
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
        updatePublisher?.onNext(app)
    }

    private fun updateToastDuration(durationInMs: Int) {
        preferences?.setToastDuration(durationInMs)
        updatePublisher?.onNext("toast")
        toastDuration?.text = (preferences!!.getToastDuration()/1000).toString()
    }

    private fun showUpdatePreferenceSnackbar() {
        root?.let {
            Snackbar.make(it, R.string.settings_preference_update_content, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.settings_clear_history_dialog_title)
                .setMessage(R.string.settings_clear_history_dialog_content)
                .setNegativeButton(R.string.settings_clear_history_dialog_cta) { _, _ -> clearHistory() }
                .setNeutralButton(android.R.string.no) {d, _ -> d.dismiss()}
                .show()
    }

    private fun showDeleteDataDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.settings_delete_data_dialog_title)
                .setMessage(R.string.settings_delete_data_dialog_content)
                .setNegativeButton(R.string.settings_delete_data_dialog_cta) { _, _ -> deleteData() }
                .setNeutralButton(android.R.string.no) {d, _ -> d.dismiss()}
                .show()
    }

    private fun clearHistory() {
        DbRecentlyBrowsedStore(MovieRatingsApplication.database.recentlyBrowsedDao())
                .deleteAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showSnackbar(R.string.settings_history_cleared)
                }, {
                    it.printStackTrace()
                    showSnackbar(R.string.settings_clear_history_error)
                })
    }

    private fun deleteData() {
        val collectionStore = DbMovieCollectionStore(MovieRatingsApplication.database.movieCollectionDao())
        Observable.concat(
                DbRecentlyBrowsedStore(MovieRatingsApplication.database.recentlyBrowsedDao()).deleteAll(),
                DbLikeStore(MovieRatingsApplication.database.favDao()).deleteAll(),
                collectionStore.deleteAllCollectionEntries(),
                collectionStore.deleteAllCollections())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {
                    it.printStackTrace()
                    showSnackbar(R.string.settings_data_delete_error)
                }, {
                    showSnackbar(R.string.settings_data_deleted)
                })
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