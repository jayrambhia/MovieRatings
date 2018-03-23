package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.ResultBus
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPageFragment
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore

class CollectionListPageFragment : BaseFragment(), CollectionListPage {

    private var emptyContent: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: CollectionListPageAdapter? = null
    private var fab: View? = null
    private var presenter: CollectionListPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = CollectionListPresenter(
                DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                DbMovieCollectionStore(MovieRatingsApplication.database.movieCollectionDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.movie_collection_list_page_layout, container, false)
        emptyContent = root.findViewById(R.id.empty_state_view)
        fab = root.findViewById(R.id.fab)
        recyclerView = root.findViewById(R.id.recyclerview)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        root.findViewById<View>(R.id.empty_cta)?.setOnClickListener {
            onCreateCollectionRequested()
        }

        fab?.setOnClickListener {
            onCreateCollectionRequested()
        }

        return root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CollectionListPageAdapter(context,
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
        when(state.ui) {
            CollectionListPage.Ui.DEFAULT -> return
            CollectionListPage.Ui.LOADING -> return
            CollectionListPage.Ui.ERROR -> return
            CollectionListPage.Ui.DATA_LOADED -> {
                fab?.visibility = View.VISIBLE
                emptyContent?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                adapter?.updateData(state.data!!)
                adapter?.notifyDataSetChanged()
            }
            CollectionListPage.Ui.EMPTY -> {
                fab?.visibility = View.GONE
                emptyContent?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
            }

        }

    }

    override fun updateState(state: CollectionListPage.OpState) {
        val resId = when(state.op) {
            CollectionListPage.Op.COLLECTION_CREATED -> R.string.movie_collection_list_page_create_success
            CollectionListPage.Op.COLLECTION_DELETED -> R.string.movie_collection_list_page_delete_success
            CollectionListPage.Op.COLLECTION_CREATE_ERROR -> R.string.movie_collection_list_page_create_error
            CollectionListPage.Op.COLLECTION_DELETE_ERROR -> R.string.movie_collection_list_page_delete_error
        }

        showSnackbar(context.getString(resId, state.data))
    }

    private fun onCreateCollectionRequested() {

        var edittext: EditText? = null

        val dialog = AlertDialog.Builder(context)
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
        AlertDialog.Builder(context)
                .setTitle(R.string.movie_collection_delete_dialog_title)
                .setMessage(context.getString(R.string.movie_collection_delete_dialog_content, collection.name))
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

    override fun canGoBack(): Boolean {
        return true
    }

    private fun shouldReturnSelection(): Boolean {
        return (path as CollectionListPagePath).returnSelection
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
    }
}