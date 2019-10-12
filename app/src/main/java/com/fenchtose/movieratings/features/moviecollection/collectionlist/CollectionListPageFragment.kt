package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPagePath
import com.fenchtose.movieratings.model.db.movieCollection.AddToCollection
import com.fenchtose.movieratings.model.db.movieCollection.CreateCollection
import com.fenchtose.movieratings.model.db.movieCollection.DeleteCollection
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.offline.export.ExportData
import com.fenchtose.movieratings.util.show

class CollectionListPageFragment: BaseFragment() {

    private var emptyContent: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: CollectionListPageAdapter? = null
    private var fab: View? = null
    private var isEmpty: Boolean = true

    override fun canGoBack() = true
    override fun getScreenTitle(): Int {
        return if (shouldReturnSelection())
            R.string.movie_collection_list_page_selection_title
        else
            R.string.movie_collection_list_page_title
    }
    override fun screenName() = AppScreens.COLLECTION_LIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.movie_collection_list_page_layout, container, false)
        emptyContent = root.findViewById(R.id.empty_state_view)
        fab = root.findViewById(R.id.fab)
        recyclerView = root.findViewById(R.id.recyclerview)
        recyclerView?.layoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )

        root.findViewById<View>(R.id.empty_cta)?.setOnClickListener {
            AppEvents.TAP_CREATE_COLLECTION.track()
            onCreateCollectionRequested()
        }

        fab?.setOnClickListener {
            AppEvents.TAP_CREATE_COLLECTION.track()
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

        render { appState, dispatch -> render(appState.collectionListPage, dispatch)}
    }

    override fun onResume() {
        super.onResume()
        dispatch?.invoke(LoadCollections)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var consumed = true
        when(item.itemId) {
            R.id.action_share -> {
                showShareDialog()
            }
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    private fun render(state: CollectionListPageState, dispatch: Dispatch) {
        isEmpty = state.collections.isEmpty()

        when(state.progress) {
            is Error -> {}// TODO
            is Progress.Loaded -> {
                if (state.collections.isEmpty()) {
                    fab?.show(false)
                    emptyContent?.show(true)
                    recyclerView?.show(false)
                } else {
                    fab?.show(true)
                    emptyContent?.show(false)
                    recyclerView?.show(true)
                }

                adapter?.updateData(state.collections)
                adapter?.notifyDataSetChanged()
            }
        }

        state.collectionOp?.let {
            val resId = when(it) {
                is CollectionOp.Created -> R.string.movie_collection_list_page_create_success
                is CollectionOp.Deleted -> R.string.movie_collection_list_page_delete_success
                is CollectionOp.CreateError -> R.string.movie_collection_list_page_create_error
                is CollectionOp.DeleteError -> R.string.movie_collection_list_page_delete_error
            }
            showSnackbar(requireContext().getString(resId, it.name))
            dispatch(ClearCollectionOp)
        }

        state.shareSuccess?.let {
            if (!it) {
                showSnackbar(R.string.movie_collection_list_share_error)
            }

            dispatch(ClearShareError)
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
                        AppEvents.CREATE_COLLECTION.track()
                        dispatch?.invoke(CreateCollection(it))
                        dialog.dismiss()
                    }
                }.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()

        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.isEnabled = false

        edittext = dialog.findViewById(R.id.edit_collection_name)

        edittext?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) { button.isEnabled = s.isNotEmpty() }

        })

    }

    private fun onCollectionDeleteRequested(collection: MovieCollection) {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.movie_collection_delete_dialog_title)
                .setMessage(requireContext().getString(R.string.movie_collection_delete_dialog_content, collection.name))
                .setNegativeButton(R.string.movie_collection_delete_dialog_negative) { _, _ ->
                    AppEvents.DELETE_COLLECTION.track()
                    dispatch?.invoke(DeleteCollection(collection))
                }
                .setNeutralButton(R.string.movie_collection_delete_dialog_neutral) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun onCollectionSelected(collection: MovieCollection) {
        if (shouldReturnSelection()) {
            AppEvents.SELECT_COLLECTION.track()
            path?.let {
                AppEvents.addToCollection(GaCategory.MOVIE).track()
                (it as CollectionListPath).movieToBeAdded?.let {
                    dispatch?.invoke(AddToCollection(collection, it))
                }
            }
            path?.getRouter()?.onBackRequested()
        } else {
            AppEvents.openCollection(path?.category()).track()
            path?.getRouter()?.let {
                dispatch?.invoke(Navigation(it, CollectionPagePath(collection)))
            }
        }
    }

    private fun showShareDialog() {
        if (isEmpty) {
            showSnackbar(R.string.movie_collection_list_share_empty)
            return
        }

        AlertDialog.Builder(requireContext())
                .setTitle(R.string.movie_collection_list_share_dialog_title)
                .setMessage(R.string.movie_collection_list_share_dialog_content)
                .setPositiveButton(R.string.movie_collection_list_share_dialog_positive_cta) {
                    dialog, _ ->
                    dialog.dismiss()
                    AppEvents.SHARE_COLLECTIONS.track()
                    dispatch?.invoke(ExportData(COLLECTION_LIST, "flutter_collections.txt",
                            DataExporter.Config(favs = false, collections = true, recentlyBrowsed = false)))
                }
                .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun shouldReturnSelection(): Boolean {
        path?.let {
            return (it as CollectionListPath).returnSelection
        }

        return false
    }
}

class CollectionListPath(val returnSelection: Boolean = false, val movieToBeAdded: Movie? = null): RouterPath<CollectionListPageFragment>() {
    override fun createFragmentInstance() = CollectionListPageFragment()
    override fun category() = GaCategory.COLLECTION_LIST
    override fun showMenuIcons(): IntArray {
        return if (returnSelection) super.showMenuIcons() else intArrayOf(R.id.action_share)
    }

    override fun initAction() = InitAction
    override fun clearAction() = ClearAction
}