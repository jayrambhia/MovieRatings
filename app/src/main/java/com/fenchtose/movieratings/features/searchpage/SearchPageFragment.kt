package com.fenchtose.movieratings.features.searchpage

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.info.InfoPageBottomView
import com.fenchtose.movieratings.features.moviepage.MoviePath
import com.fenchtose.movieratings.model.db.like.LikeMovie
import com.fenchtose.movieratings.model.db.movieCollection.AddToCollection
import com.fenchtose.movieratings.model.db.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.bottomSlide
import com.fenchtose.movieratings.util.slideUp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SearchPageFragment: BaseFragment() {

    private var progressbar: ProgressBar? = null
    private var attributeView: TextView? = null
    private var searchView: EditText? = null
    private var clearButton: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: BaseMovieAdapter? = null
    private var adapterConfig: SearchAdapterConfig? = null
    private var appInfoContainer: InfoPageBottomView? = null

    private var watcher: TextWatcher? = null
    private var querySubject: PublishSubject<String>? = null

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

        path?.takeIf { it is SearchPageFragment.SearchPath.Default }?.let {
            appInfoContainer = view.findViewById(R.id.info_page_container)
            appInfoContainer?.setRouter(it.getRouter(), it.category())
            view.findViewById<View?>(R.id.settings_view)?.visibility = View.VISIBLE
            view.findViewById<View?>(R.id.credit_view)?.visibility = View.GONE
        }

        val adapterConfig = SearchAdapterConfig(GlideLoader(Glide.with(this)),
                object: SearchAdapterConfig.SearchCallback {
                    override fun onLiked(movie: Movie) {
                        GaEvents.LIKE_MOVIE.withCategory(path?.category()).track()
                        dispatch?.invoke(LikeMovie(movie, !movie.liked))
                    }

                    override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                        GaEvents.OPEN_MOVIE.withCategory(path?.category()).track()
                        path?.getRouter()?.let {
                            dispatch?.invoke(Navigation(it, MoviePath(movie, sharedElement)))
                        }
                    }

                    override fun onLoadMore() {
                        dispatch?.invoke(SearchAction.LoadMore(path is SearchPath.AddToCollection))
                    }
                },
                createExtraLayoutHelper())

        val adapter = BaseMovieAdapter(requireContext(), adapterConfig)
        adapterConfig.adapter = adapter

        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter
        this.adapterConfig = adapterConfig

        clearButton?.setOnClickListener {
            clearQuery()
            searchView?.setText("")
            GaEvents.CLEAR_SEARCH.withCategory(path?.category()).track()
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
                            dispatch?.invoke(SearchAction.Search(it, path is SearchPath.AddToCollection))
                        }
                )

        subscribe(d)
        searchView?.addTextChangedListener(watcher)
        this.querySubject = subject

        render({
            state, dispatch ->
            if (path is SearchPath.AddToCollection) {
                render(state.collectionSearchPage, dispatch)
            } else {render(state.searchPage, dispatch) }
        })
    }

    private fun render(state: SearchPageState, dispatch: Dispatch) {
        this.dispatch = dispatch
        if (state.query != searchView?.text?.toString()) {
            searchView?.setText(state.query)
        }

        when(state.progress) {
            is Progress.Default -> {
                clearData()
            }

            is Progress.Loading -> {
                showLoading(true)
                appInfoContainer?.bottomSlide(500)
            }

            is Progress.Error, is Progress.PaginationError -> showApiError()

            is Progress.Success -> {
                setData(state.movies)
                appInfoContainer?.bottomSlide(500)
                fixScroll(state.progress is Progress.Success.Loaded)
            }

            is Progress.Paginating -> adapterConfig?.showLoadingMore(true)
        }
    }

    private fun render(state: CollectionSearchPageState, dispatch: Dispatch) {
        render(state.searchPageState, dispatch)
        val resId = when(state.collectionProgress) {
            is CollectionProgress.Default -> 0
            is CollectionProgress.Exists -> R.string.movie_collection_movie_exists
            is CollectionProgress.Error -> R.string.movie_collection_movie_error
            is CollectionProgress.Added -> R.string.movie_collection_movie_added
        }

        if (resId != 0) {
            showSnackbar(requireContext().getString(resId, state.collectionProgress.collection))
            val _path = path
            if (_path is SearchPageFragment.SearchPath.AddToCollection) {
                dispatch.invoke(CollectionSearchAction.ClearCollectionOp(_path.collection))
            }
        }
    }

    private fun clearData() {
        showLoading(false)
        appInfoContainer?.slideUp(500)
        clearButton?.visibility = View.GONE
        adapter?.data?.clear()
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            recyclerView?.visibility = View.GONE
        }
    }

    private fun fixScroll(isFirstPage: Boolean) {
        recyclerView?.post {
            when(isFirstPage) {
                true -> recyclerView?.scrollToPosition(0)
                false -> adapterConfig?.showLoadingMore(false)
            }
        }
    }

    private fun setData(movies: List<Movie>) {
        showLoading(false)
        adapter?.data?.clear()
        adapter?.data?.addAll(movies)
        adapter?.notifyDataSetChanged()
        recyclerView?.visibility = View.VISIBLE

    }

    private fun clearQuery() {
        dispatch?.invoke(SearchAction.ClearSearch(path is SearchPath.AddToCollection))
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

    private fun showApiError() {
        showLoading(false)
        adapterConfig?.showLoadingMore(false)
        showSnackbarWithAction(requireContext().getString(R.string.search_page_api_error_content),
                R.string.search_page_try_again_cta,
                View.OnClickListener {
                    dispatch?.invoke(SearchAction.Reload(searchView?.text?.toString()?: "", path is SearchPath.AddToCollection))
                })
    }

    private fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? {
        return when(path) {
            is SearchPath.AddToCollection -> {
                return ::createAddToCollectionExtraLayout
            }

            else -> null
        }
    }

    private fun createAddToCollectionExtraLayout(): SearchItemViewHolder.ExtraLayoutHelper {
        return AddToCollectionMovieLayoutHelper(object : AddToCollectionMovieLayoutHelper.Callback {
            override fun onAddRequested(movie: Movie) {
                path?.takeIf { it is SearchPath.AddToCollection }?.let {
                    dispatch?.invoke(AddToCollection((it as SearchPath.AddToCollection).collection, movie))
                }
            }
        })
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return when(path) {
            is SearchPageFragment.SearchPath.AddToCollection -> R.string.search_page_add_to_collection_title
            else -> R.string.search_page_title
        }
    }

    override fun screenName(): String {
        path?.takeIf { it is SearchPath }?.let {
            return when(it as SearchPath) {
                is SearchPath.Default -> GaScreens.SEARCH
                is SearchPath.AddToCollection -> GaScreens.COLLECTION_SEARCH
            }
        }

        return GaScreens.SEARCH
    }

    sealed class SearchPath: RouterPath<SearchPageFragment>() {

        class Default(private val preferences: UserPreferences): SearchPageFragment.SearchPath() {
            override fun createFragmentInstance(): SearchPageFragment {
                return SearchPageFragment()
            }

            override fun showMenuIcons(): IntArray {
                val icons = arrayListOf(R.id.action_info, R.id.action_fav, R.id.action_collection, R.id.action_trending)
                if (preferences.isAppEnabled(UserPreferences.SAVE_HISTORY)) {
                    icons.add(R.id.action_history)
                }

                return icons.toIntArray()
            }

            override fun showBackButton() = false

            override fun category() = GaCategory.SEARCH
        }

        class AddToCollection(val collection: MovieCollection): SearchPageFragment.SearchPath() {
            override fun createFragmentInstance(): SearchPageFragment {
                return SearchPageFragment()
            }

            override fun showBackButton() = true

            override fun category() = GaCategory.COLLECTION_SEARCH

            override fun initAction() = CollectionSearchAction.InitializeCollectionSearch(collection)
        }
    }

}