package com.fenchtose.movieratings.features.settings

import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movie.DbMovieStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.offline.import.DataFileImporter
import com.fenchtose.movieratings.model.offline.import.DataImporter
import com.fenchtose.movieratings.util.AppFileUtils
import com.fenchtose.movieratings.util.Constants
import io.reactivex.android.schedulers.AndroidSchedulers

class ImportDataFragment : BaseFragment() {

    private var stateContainer: View? = null
    private var importContainer: View? = null

    private var collections: CheckBox? = null
    private var favs: CheckBox? = null
    private var recentlyBrowsed: CheckBox? = null
    private var importCta: Button? = null

    private var uri: Uri? = null
    private var importer: DataImporter? = null

    var callback: Callback? = null

    companion object {
        val EXTRA_URI = "extra.uri"
    }

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.import_screen_title

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.import_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        importCta = view.findViewById(R.id.import_button)
        importCta?.setOnClickListener {
            importData()
        }

        view.findViewById<View>(R.id.open_flutter_cta).setOnClickListener {
            callback?.openFlutter()
        }

        view.findViewById<View>(R.id.go_back_cta).setOnClickListener {
            callback?.goBack()
        }

        collections = view.findViewById(R.id.collections_checkbox)
        favs = view.findViewById(R.id.fav_checkbox)
        recentlyBrowsed = view.findViewById(R.id.recently_browsed_checkbox)

        collections?.setOnCheckedChangeListener {
            _, _ -> triggerCheck()
        }

        favs?.setOnCheckedChangeListener {
            _, _ -> triggerCheck()
        }

        recentlyBrowsed?.setOnCheckedChangeListener {
            _, _ -> triggerCheck()
        }

        stateContainer = view.findViewById(R.id.state_container)
        importContainer = view.findViewById(R.id.import_container)

        var uri = arguments?.getParcelable<Uri?>(EXTRA_URI)
        path?.takeIf { it is ImportDataPath }?.let {
            view.findViewById<View>(R.id.open_flutter_cta).visibility = View.GONE
            view.findViewById<View>(R.id.go_back_cta).visibility = View.GONE
            uri = (it as ImportDataPath).uri
        }

        if (uri != null) {
            handleImport(uri!!)
        } else {
            showState(R.string.import_screen_error_info)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        importer?.release()
    }

    private fun showState(@StringRes copy: Int) {
        stateContainer?.visibility = View.VISIBLE
        importContainer?.visibility = View.GONE
        stateContainer?.findViewById<TextView>(R.id.state_info)?.setText(copy)
    }

    private fun handleImport(uri: Uri) {
        this.uri = uri
        importer = DataFileImporter(context,
                AppFileUtils(),
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                DbRecentlyBrowsedStore.getInstance(MovieRatingsApplication.database.recentlyBrowsedDao()),
                DbMovieStore.getInstance(MovieRatingsApplication.database.movieDao()))

        importer?.let {
            it.observe()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        when(it) {
                            is DataImporter.Progress.Started -> {}
                            is DataImporter.Progress.Success -> {
                                showState(R.string.import_screen_success_info)
                            }
                            is DataImporter.Progress.Error -> {
                                showState(R.string.import_screen_error_info)
                            }
                        }
                    }, {

                    })

            it.report(uri)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showImports(it)
                    }, {
                        showState(R.string.import_screen_error_info)
                    })
        }

    }

    private fun showImports(report: DataImporter.Report) {
        if (report.name != Constants.EXPORT_APP_NAME) {
            showState(R.string.import_screen_error_unsupported_file)
            return
        }

        if (report.version == null) {
            showState(R.string.import_screen_error_unsupported_version)
            return
        }

        val count = updateCheckbox(report.favs, favs) +
                updateCheckbox(report.recentlyBrowsed, recentlyBrowsed) +
                updateCheckbox(report.collections, collections)

        if (count == 3) {
            showState(R.string.import_screen_error_no_data_file)
            return
        }

        importContainer?.visibility = View.VISIBLE
    }

    private fun updateCheckbox(status: Boolean, view: CheckBox?): Int {
        return if (status) {
            view?.isEnabled = true
            view?.visibility = View.VISIBLE
            0
        } else {
            view?.isEnabled = false
            view?.visibility = View.GONE
            1
        }
    }

    private fun checkCheckbox(view: CheckBox?): Int {
        return if (view?.isEnabled == true && view?.isChecked == true) 1 else 0
    }

    private fun triggerCheck() {
        val count = checkCheckbox(favs) + checkCheckbox(collections) + checkCheckbox(recentlyBrowsed)
        importCta?.isEnabled = count > 0
    }

    private fun importData() {
        val config = DataImporter.Config(
                checkCheckbox(favs) == 1,
                checkCheckbox(collections) == 1,
                checkCheckbox(recentlyBrowsed) == 1)
        uri?.let {
            importer?.import(it, config)
        }
    }

    class ImportDataPath(val uri: Uri): RouterPath<ImportDataFragment>() {
        override fun createFragmentInstance(): ImportDataFragment {
            return ImportDataFragment()
        }
    }

    interface Callback {
        fun openFlutter()
        fun goBack()
    }
}