package com.fenchtose.movieratings.features.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movie.DbMovieStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.offline.export.DataFileExporter
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AppFileUtils
import com.fenchtose.movieratings.util.IntentUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DataSectionFragment: BaseFragment() {

    private val REQUEST_OPEN_FILE = 32
    private val REQUEST_CREATE_FILE = 33

    private var updatePublisher: PreferenceUpdater? = null

    private var historyCheckboxSelected: Boolean? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.settings_data_section_page_header
    override fun screenName() = AppScreens.SETTINGS_DATA_SECTION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_data_section_page_layout, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = SettingsPreferences(requireContext())

        updatePublisher = PreferenceUpdater(view as ViewGroup)

        addAppToggle(preferences, view, R.id.save_browsing_toggle, UserPreferences.SAVE_HISTORY)
        view.findViewById<View>(R.id.clear_history_button).setOnClickListener { showClearHistoryDialog() }
        view.findViewById<View>(R.id.delete_data_button).setOnClickListener { showDeleteDataDialog() }
        view.findViewById<View>(R.id.export_data_button).setOnClickListener { showExportDataDialog() }
        view.findViewById<View>(R.id.import_data_button).setOnClickListener { showImportDataDialog() }
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
        preferences.setEnabled(app, checked)
        updatePublisher?.show(app)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_OPEN_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                    importData(data.data)
                }
            }
            REQUEST_CREATE_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                    exportData(data.data, historyCheckboxSelected == true)
                }
                historyCheckboxSelected = null
            }
        }
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_clear_history_dialog_title)
                .setMessage(R.string.settings_clear_history_dialog_content)
                .setNegativeButton(R.string.settings_clear_history_dialog_cta) { _, _ -> clearHistory() }
                .setNeutralButton(android.R.string.no) {d, _ -> d.dismiss()}
                .show()
    }

    private fun showDeleteDataDialog() {
        AlertDialog.Builder(requireContext())
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

        val observables = arrayListOf(
                DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao()).deleteAll(),
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()).deleteAll(),
                DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao()).deleteAll(),
                collectionStore.deleteAllCollectionEntries(),
                collectionStore.deleteAllCollections())

        Observable.concat(observables)
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
        var historyCheckbox: CheckBox? = null
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_export_data_dialog_title)
                .setView(R.layout.export_data_dialog_layout)
                .setPositiveButton(R.string.settings_export_data_dialog_positive_cta) {
                    dialog, _ ->
                    historyCheckboxSelected = historyCheckbox?.isChecked
                    createFile()
                    dialog.dismiss()
                }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()

        historyCheckbox = dialog.findViewById(R.id.include_history_checkbox)
    }

    private fun createFile() {
        startActivityForResult(Intent.createChooser(
                IntentUtils.getFileCreationIntent("flutter_data.txt"), "Save file via"),
                REQUEST_CREATE_FILE)
    }

    private fun exportData(uri: Uri, includeHistory: Boolean) {
        val exporter = DataFileExporter(
                AppFileUtils(),
                DbMovieStore.getInstance(MovieRatingsApplication.database.movieDao()),
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao()))

        subscribe(exporter.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it.key == "all data" }
                .subscribe({
                    when(it) {
                        is DataExporter.Progress.Started -> {}
                        is DataExporter.Progress.Error -> showSnackbar(R.string.settings_export_data_error)
                        is DataExporter.Progress.Success -> showExportDataReady(it.output)
                    }
                }, {
                    exporter.release()
                }, {
                    exporter.release()
                }))

        exporter.export("all data", uri, DataExporter.Config(true, true, includeHistory))
    }

    private fun showExportDataReady(uri: Uri) {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_export_data_ready_dialog_title)
                .setMessage(requireContext().getString(R.string.settings_export_data_ready_dialog_content))
                .setPositiveButton(R.string.settings_export_data_ready_dialog_positive_cta) {
                    dialog, _ ->
                        dialog.dismiss()
                        IntentUtils.openShareFileIntent(requireContext(), uri)
                }
                .setNeutralButton(android.R.string.ok) {
                    dialog, _ -> dialog.dismiss()
                }
                .show()
    }

    private fun showImportDataDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_import_data_dialog_title)
                .setMessage(R.string.settings_import_data_dialog_content)
                .setPositiveButton(R.string.settings_import_data_dialog_positive_cta) {
                    dialog, _ -> startFileSelection()
                    dialog.dismiss()
                }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun startFileSelection() {
        startActivityForResult(Intent.createChooser(IntentUtils.getFileSelectionIntent(), "Find file via"), REQUEST_OPEN_FILE)
    }

    private fun importData(uri: Uri) {
        path?.getRouter()?.go(ImportDataFragment.ImportDataPath(uri))
    }

    class DataSettingsPath: RouterPath<DataSectionFragment>() {
        override fun createFragmentInstance(): DataSectionFragment {
            return DataSectionFragment()
        }

    }
}