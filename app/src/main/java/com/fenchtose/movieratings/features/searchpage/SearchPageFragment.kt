package com.fenchtose.movieratings.features.searchpage

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.bottomSlide
import com.fenchtose.movieratings.util.slideUp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SearchPageFragment : BaseFragment(), SearchPage {

    private var progressbar: ProgressBar? = null
    private var attributeView: TextView? = null
    private var searchView: EditText? = null
    private var clearButton: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: BaseMovieAdapter? = null
    private var adapterConfig: SearchAdapterConfig? = null
    private var appInfoContainer: View? = null

    private var presenter: SearchPresenter? = null

    private var watcher: TextWatcher? = null
    private var querySubject: PublishSubject<String>? = null

//    private var state: SearchPage.State = SearchPage.State.Default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val likeStore = DbLikeStore.getInstance(MovieRatingsApplication.database.favDao())
        path?.let {
            presenter = when(it as SearchPath) {
                is SearchPath.Default -> SearchPresenter.DefaultPresenter(
                        MovieRatingsApplication.movieProviderModule.movieProvider,
                        likeStore)
                is SearchPath.AddToCollection -> SearchPresenter.AddToCollectionPresenter(
                        MovieRatingsApplication.movieProviderModule.movieProvider,
                        likeStore,
                        DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                        (it as SearchPath.AddToCollection).collection)
            }
        }

        presenter?.restoreState(path?.restoreState())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.search_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressbar = view.findViewById(R.id.progressbar)
        attributeView = view.findViewById(R.id.api_attr)
        recyclerView = view.findViewById(R.id.recyclerview)
        searchView = view.findViewById(R.id.search_view)
        clearButton = view.findViewById(R.id.clear_button)

        path?.takeIf { it is SearchPath.Default }?.let {
            appInfoContainer = view.findViewById(R.id.info_page_container)
            view.findViewById<View?>(R.id.settings_view)?.visibility = View.VISIBLE
            view.findViewById<View?>(R.id.credit_view)?.visibility = View.GONE
        }

        val adapterConfig = SearchAdapterConfig(GlideLoader(Glide.with(this)),
                object: SearchAdapterConfig.SearchCallback {
                        override fun onLiked(movie: Movie) {
                            presenter?.setLiked(movie)
                        }

                        override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                            presenter?.openMovie(movie, sharedElement)
                        }

                        override fun onLoadMore() {
                            presenter?.loadMore()
                        }
                    },
                    createExtraLayoutHelper())

        val adapter = BaseMovieAdapter(context, adapterConfig)
        adapterConfig.adapter = adapter

        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter
        this.adapterConfig = adapterConfig

        presenter?.attachView(this)

        clearButton?.setOnClickListener {
            clearQuery()
            searchView?.setText("")
        }

        watcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                querySubject?.onNext(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        }

        val subject: PublishSubject<String> = PublishSubject.create()
        val d = subject
                .doOnNext {
                    if (it.isEmpty()) {
                        clearQuery()
                    } else {
                        clearButton?.visibility = View.VISIBLE
                    }
                }
                .debounce(800, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it.length > 2 }
                .subscribeBy(
                        onNext = {
                            presenter?.onSearchRequested(it)
                        }
                )

        subscribe(d)
        searchView?.addTextChangedListener(watcher)
        this.querySubject = subject
    }

    override fun saveState(): PresenterState? {
        return presenter?.saveState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
        clearButton?.setOnClickListener(null)
        watcher?.let {
            searchView?.removeTextChangedListener(it)
            watcher = null
        }
        querySubject?.onComplete()
        recyclerView?.adapter = null
        adapter = null
        adapterConfig = null
    }

    override fun onDestroy() {
        super.onDestroy()
        MovieRatingsApplication.refWatcher?.watch(this)
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        path?.takeIf { it is SearchPath }?.let {
            return when(it as SearchPath) {
                is SearchPath.Default -> R.string.search_page_title
                is SearchPath.AddToCollection -> R.string.search_page_add_to_collection_title
            }
        }
        return R.string.search_page_title
    }

    override fun updateState(state: SearchPage.State) {
//        if (this.state == state) {
//            return
//        }

        when (state) {
            is SearchPage.State.Default -> {
                clearQuery()
            }
            is SearchPage.State.Loading -> {
                showLoading(true)
                appInfoContainer?.bottomSlide(500)
            }
            is SearchPage.State.Loaded -> {
                setData(state)
                appInfoContainer?.bottomSlide(500)
            }
            is SearchPage.State.NoResult -> showNoResultError()
            is SearchPage.State.Error -> showApiError()
            is SearchPage.State.LoadingMore -> adapterConfig?.showLoadingMore(true)
            is SearchPage.State.PaginationError -> showApiError()
        }
    }

    override fun updateState(state: SearchPage.CollectionState) {
        val resId = when(state) {
            is SearchPage.CollectionState.Exists -> R.string.movie_collection_movie_exists
            is SearchPage.CollectionState.Added -> R.string.movie_collection_movie_added
            is SearchPage.CollectionState.Error -> R.string.movie_collection_movie_error
        }

        showSnackbar(context.getString(resId, state.collection.name))
    }

    private fun showLoading(status: Boolean) {
        if (status) {
            progressbar?.visibility = View.VISIBLE
            attributeView?.visibility = View.GONE
            recyclerView?.visibility = View.GONE
        } else {
            attributeView?.visibility = View.VISIBLE
            progressbar?.visibility = View.GONE
        }
    }

    private fun showNoResultError() {
        showLoading(false)
        adapterConfig?.showLoadingMore(false)
        showSnackbar(R.string.search_page_empty_result)
    }

    private fun showApiError() {
        showLoading(false)
        adapterConfig?.showLoadingMore(false)
        showSnackbarWithAction(context.getString(R.string.search_page_api_error_content),
                R.string.search_page_try_again_cta,
                View.OnClickListener {
                    presenter?.retrySearch()
                })
    }

    private fun setData(state: SearchPage.State.Loaded) {
        showLoading(false)
        adapter?.data = state.movies
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            when(state) {
                is SearchPage.State.Loaded.PaginationSuccess -> adapterConfig?.showLoadingMore(false)
                is SearchPage.State.Loaded.Restored -> {}
                is SearchPage.State.Loaded.Success -> recyclerView?.scrollToPosition(0)
            }
        }
        recyclerView?.visibility = View.VISIBLE
    }

    private fun clearQuery() {
        clearData()
        showLoading(false)
        appInfoContainer?.slideUp(500)
        presenter?.onQueryCleared()
        clearButton?.visibility = View.GONE
    }

    private fun clearData() {
        adapter?.data = ArrayList()
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            recyclerView?.visibility = View.GONE
        }
    }

    private fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? {
        presenter?.let {
            return when(it) {
                is SearchPresenter.DefaultPresenter -> null
                is SearchPresenter.AddToCollectionPresenter -> {
                    return ::createAddToCollectionExtraLayout
                }
            }
        }
        return null
    }

    private fun createAddToCollectionExtraLayout(): SearchItemViewHolder.ExtraLayoutHelper {
        return AddToCollectionMovieLayoutHelper(object : AddToCollectionMovieLayoutHelper.Callback {
            override fun onAddRequested(movie: Movie) {
                presenter?.takeIf { it is SearchPresenter.AddToCollectionPresenter }
                                ?.let { it as SearchPresenter.AddToCollectionPresenter }?.addToCollection(movie)
            }
        })
    }

    sealed class SearchPath: RouterPath<SearchPageFragment>() {
        class Default(private val preferences: UserPreferences): SearchPath() {
            override fun createFragmentInstance(): SearchPageFragment {
                return SearchPageFragment()
            }

            override fun showMenuIcons(): IntArray {
                val icons = arrayListOf(R.id.action_info, R.id.action_fav, R.id.action_collection)
                if (preferences.isAppEnabled(UserPreferences.SAVE_HISTORY)) {
                    icons.add(R.id.action_history)
                }

                return icons.toIntArray()
            }

            override fun showBackButton() = false
        }

        class AddToCollection(val collection: MovieCollection): SearchPath() {
            override fun createFragmentInstance(): SearchPageFragment {
                return SearchPageFragment()
            }

            override fun showBackButton() = true
        }
    }

}
