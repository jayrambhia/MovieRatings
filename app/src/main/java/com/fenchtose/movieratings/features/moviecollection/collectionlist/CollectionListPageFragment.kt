package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.ResultBus
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPageFragment
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.offline.export.DataFileExporter
import com.fenchtose.movieratings.util.AppFileUtils
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.IntentUtils

class CollectionListPageFragment : BaseFragment(), CollectionListPage {

    private var emptyContent: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: CollectionListPageAdapter? = null
    // TODO fab should move based on Snackbar
    private var fab: View? = null
    private var presenter: CollectionListPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CollectionListPresenter(
                requireContext(),
                AppRxHooks(),
                AppFileUtils(),
                DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                DataFileExporter.newInstance(MovieRatingsApplication.database))
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.movie_collection_list_page_layout, container, false)
        emptyContent = root.findViewById(R.id.empty_state_view)
        fab = root.findViewById(R.id.fab)
        recyclerView = root.findViewById(R.id.recyclerview)
        recyclerView?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        root.findViewById<View>(R.id.empty_cta)?.setOnClickListener {
            onCreateCollectionRequested()
        }

        fab?.setOnClickListener {
            onCreateCollectionRequested()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CollectionListPageAdapter(requireContext(),
                GlideLoader(Glide.with(this)),
                object: CollectionListPageAdapter.AdapterCallback {
                    override fun onDeleteRequested(collection: MovieCollection) {
                        onCollectionDeleteRequested(collection)
                    }

                    override fun onClicked(collection: MovieCollection) {
                        onCollectionSelected(collection)
                    }
                },
                !shouldReturnSelection())

        adapter?.let {
            it.setHasStableIds(true)
            recyclerView?.adapter = adapter
        }

        presenter?.attachView(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
    }

    override fun updateState(state: CollectionListPage.State) {
        when(state) {
            is CollectionListPage.State.Success -> {
                fab?.visibility = View.VISIBLE
                emptyContent?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                adapter?.updateData(state.collections)
                adapter?.notifyDataSetChanged()
            }
            is CollectionListPage.State.Empty-> {
                fab?.visibility = View.GONE
                emptyContent?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
            }

        }

    }

    override fun updateState(state: CollectionListPage.OpState) {
        val resId = when(state) {
            is CollectionListPage.OpState.Created -> R.string.movie_collection_list_page_create_success
            is CollectionListPage.OpState.Deleted -> R.string.movie_collection_list_page_delete_success
            is CollectionListPage.OpState.CreateError -> R.string.movie_collection_list_page_create_error
            is CollectionListPage.OpState.DeleteError -> R.string.movie_collection_list_page_delete_error
        }

        showSnackbar(requireContext().getString(resId, state.data))
    }

    override fun updateState(state: CollectionListPage.ShareState) {
        when(state) {
            is CollectionListPage.ShareState.Started -> {}
            is CollectionListPage.ShareState.Error -> showSnackbar(R.string.movie_collection_list_share_error)
            is CollectionListPage.ShareState.Success -> IntentUtils.openShareFileIntent(requireContext(), state.uri)
        }
    }

    private fun onCreateCollectionRequested() {

        var edittext: EditText? = null

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.movie_collection_create_dialog_title)
                .setView(R.layout.create_collection_dialog_layout)
                .setPositiveButton(R.string.movie_collection_create_dialog_positive_cta) {
                    dialog, _ ->
                    val name = edittext?.text.toString().trim()
                    name.takeIf { it.isNotEmpty() }?.let {
                        presenter?.createCollection(it)
                        dialog.dismiss()
                    }
                }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()

        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.isEnabled = false

        edittext = dialog.findViewById(R.id.edit_collection_name)

        edittext?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                button.isEnabled = s.isNotEmpty()
            }

        })

    }

    private fun onCollectionDeleteRequested(collection: MovieCollection) {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.movie_collection_delete_dialog_title)
                .setMessage(requireContext().getString(R.string.movie_collection_delete_dialog_content, collection.name))
                .setNegativeButton(R.string.movie_collection_delete_dialog_negative) { _, _ -> presenter?.deleteCollection(collection) }
                .setNeutralButton(R.string.movie_collection_delete_dialog_neutral) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun onCollectionSelected(collection: MovieCollection) {
        if (shouldReturnSelection()) {
            MovieRatingsApplication.router?.onBackRequested()
            ResultBus.setResult(CollectionListPagePath.SELECTED_COLLECTION, ResultBus.Result.create(collection))
        } else {
            MovieRatingsApplication.router?.go(CollectionPageFragment.CollectionPagePath(collection))
        }
    }

    private fun showShareDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.movie_collection_list_share_dialog_title)
                .setMessage(R.string.movie_collection_list_share_dialog_content)
                .setPositiveButton(R.string.movie_collection_list_share_dialog_positive_cta) {
                    dialog, _ ->
                    dialog.dismiss()
                    presenter?.share()
                }
                .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    override fun canGoBack(): Boolean {
        return true
    }

    private fun shouldReturnSelection(): Boolean {
        path?.let {
            return (it as CollectionListPagePath).returnSelection
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            R.id.action_share -> showShareDialog()
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun getScreenTitle(): Int {
        return if (shouldReturnSelection())
            R.string.movie_collection_list_page_selection_title
        else
            R.string.movie_collection_list_page_title
    }

    class CollectionListPagePath(val returnSelection: Boolean) : RouterPath<CollectionListPageFragment>() {

        companion object {
            val SELECTED_COLLECTION = "selected_collection"
        }

        override fun createFragmentInstance(): CollectionListPageFragment {
            return CollectionListPageFragment()
        }

        override fun showMenuIcons(): IntArray {
            return if (returnSelection) super.showMenuIcons() else intArrayOf(R.id.action_share)
        }
    }
}