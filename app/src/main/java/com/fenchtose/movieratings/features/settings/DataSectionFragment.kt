package com.fenchtose.movieratings.features.settings

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DataSectionFragment: BasePermissionFragment() {

    private val PERMISSION_FOR_EXPORT = 31

    private var updatePublisher: PreferenceUpdater? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_data_section_page_header

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_data_section_page_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = SettingsPreferences(context)

        updatePublisher = PreferenceUpdater(view as ViewGroup)

        addAppToggle(preferences, view, R.id.save_browsing_toggle, UserPreferences.SAVE_HISTORY)
        view.findViewById<View>(R.id.clear_history_button).setOnClickListener { showClearHistoryDialog() }
        view.findViewById<View>(R.id.delete_data_button).setOnClickListener { showDeleteDataDialog() }
        view.findViewById<View>(R.id.export_data_button).setOnClickListener { showExportDataDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updatePublisher?.release()
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
        updatePublisher?.show(app)
    }

    override fun onRequestGranted(code: Int) {
        when(code) {
            PERMISSION_FOR_EXPORT -> showExportDataDialog()
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

    class DataSettingsPath: RouterPath<DataSectionFragment>() {
        override fun createFragmentInstance(): DataSectionFragment {
            return DataSectionFragment()
        }

    }
}