package com.fenchtose.movieratings.features.settings

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import com.fenchtose.movieratings.MainActivity
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BasePermissionFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movie.DbMovieStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.offline.export.DataFileExporter
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.PackageUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SettingsFragment: BasePermissionFragment() {

    private var root: ViewGroup? = null
    private var updatePublisher: PublishSubject<String>? = null

    private var preferences: UserPreferences? = null

    private var ratingDurationView: TextView? = null

    private val CHECK_TTS = 12

    private val PERMISSION_FOR_EXPORT = 31

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
        addAppToggle(preferences, view, R.id.play_movies_toggle, UserPreferences.PLAY_MOVIES)
        addAppToggle(preferences, view, R.id.save_browsing_toggle, UserPreferences.SAVE_HISTORY)
        addAppToggle(preferences, view, R.id.show_activate_toggle, UserPreferences.SHOW_ACTIVATE_FLUTTER)
        addAppToggle(preferences, view, R.id.use_year_toggle, UserPreferences.USE_YEAR)

        val ttsToggle = view.findViewById<SwitchCompat>(R.id.tts_toggle)
        ttsToggle.isChecked = preferences.isSettingEnabled(UserPreferences.TTS_AVAILABLE) && preferences.isSettingEnabled(UserPreferences.USE_TTS)
        ttsToggle.setOnCheckedChangeListener {
            toggle, isChecked ->
            run {
                if (!isChecked) {
                    this.preferences?.setSettingEnabled(UserPreferences.USE_TTS, false)
                    updatePublisher?.onNext(UserPreferences.USE_TTS)
                    return@setOnCheckedChangeListener
                }

                if (this.preferences?.isSettingEnabled(UserPreferences.TTS_AVAILABLE) == true) {
                    this.preferences?.setSettingEnabled(UserPreferences.USE_TTS, true)
                    updatePublisher?.onNext(UserPreferences.USE_TTS)
                    return@setOnCheckedChangeListener
                }

                toggle.isChecked = false
                checkForTTS()
            }
        }

        val ratingDurationSeekbar = view.findViewById<SeekBar>(R.id.rating_duration_seekbar)
        ratingDurationView = view.findViewById(R.id.rating_duration_view)

        val progress = preferences.getRatingDisplayDuration()/1000
        ratingDurationView?.text = (progress).toString()
        ratingDurationSeekbar?.progress = progress - 1

        ratingDurationSeekbar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateRatingDisplayDuration((progress + 1) * 1000)
            }
        })

        view.findViewById<View>(R.id.clear_history_button).setOnClickListener { showClearHistoryDialog() }
        view.findViewById<View>(R.id.delete_data_button).setOnClickListener { showDeleteDataDialog() }
        view.findViewById<View>(R.id.export_data_button).setOnClickListener { showExportDataDialog() }

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

    private fun updateRatingDisplayDuration(durationInMs: Int) {
        preferences?.setRatingDisplayDuration(durationInMs)
        updatePublisher?.onNext("toast")
        ratingDurationView?.text = (preferences!!.getRatingDisplayDuration()/1000).toString()
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
        DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao())
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
        val collectionStore = DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao())
        Observable.concat(
                DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao()).deleteAll(),
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()).deleteAll(),
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

    private fun showExportDataDialog() {

        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (!hasPermission(permission)) {
            showRationaleDialog(R.string.settings_export_data_permission_title,
                    R.string.settings_export_data_permission_content,
                    permission,
                    PERMISSION_FOR_EXPORT)
            return
        }

        var historyCheckbox: CheckBox? = null
        val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.settings_export_data_dialog_title)
                .setView(R.layout.export_data_dialog_layout)
                .setPositiveButton(R.string.settings_export_data_dialog_positive_cta) {
                    dialog, _ ->
                        exportData(historyCheckbox?.isChecked ?: false)
                        dialog.dismiss()
                }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()

        historyCheckbox = dialog.findViewById(R.id.include_history_checkbox)
    }

    private fun exportData(includeHistory: Boolean) {
        val exported = DataFileExporter(
                DbMovieStore.getInstance(MovieRatingsApplication.database.movieDao()),
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao()))
        exported.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    progress -> Log.d("SettingsFragment", progress.toString())
                })

        exported.export(DataExporter.Config(includeHistory))
    }

    private fun checkForTTS() {
        val intent = Intent().setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        if (PackageUtils.isIntentCallabale(context, intent)) {
            startActivityForResult(intent, CHECK_TTS)
        } else {
            showSnackbar(R.string.settings_install_tts_unsupported)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_TTS) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                preferences?.setSettingEnabled(UserPreferences.TTS_AVAILABLE, true)
            } else {
                showTtsInstallDialog()
            }
        }
    }

    private fun showTtsInstallDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.settings_install_tts_dialog_title)
                .setMessage(R.string.settings_install_tts_dialog_content)
                .setPositiveButton(R.string.settings_install_tts_dialog_cta) { _, _ -> installTTS() }
                .setNeutralButton(R.string.settings_install_tts_dialog_later) {d, _ -> d.dismiss()}
                .show()
    }

    private fun installTTS() {
        val intent = Intent().setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        if (PackageUtils.isIntentCallabale(context, intent)) {
            startActivity(intent)
        } else {
            showSnackbar(R.string.settings_install_tts_unsupported)
        }
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.settings_header
    }

    override fun onRequestGranted(code: Int) {
        when(code) {
            PERMISSION_FOR_EXPORT -> showExportDataDialog()
        }
    }

    class SettingsPath: RouterPath<SettingsFragment>() {

        override fun createFragmentInstance(): SettingsFragment {
            return SettingsFragment()
        }

    }

}