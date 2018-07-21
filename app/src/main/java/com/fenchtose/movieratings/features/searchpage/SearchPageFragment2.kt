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
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.BaseMovieAdapter
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Unsubscribe
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.features.info.InfoPageBottomView
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.db.like.LikeMovie
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.bottomSlide
import com.fenchtose.movieratings.util.slideUp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class SearchPageFragment2: BaseFragment() {

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

    private var dispatch: Dispatch? = null
    private var unsubscribe: Unsubscribe? = null

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

        path?.takeIf { it is SearchPageFragment2.SearchPath2.Default }?.let {
            appInfoContainer = view.findViewById(R.id.info_page_container)
            appInfoContainer?.setRouter(it.getRouter())
            view.findViewById<View?>(R.id.settings_view)?.visibility = View.VISIBLE
            view.findViewById<View?>(R.id.credit_view)?.visibility = View.GONE
        }

        val adapterConfig = SearchAdapterConfig(GlideLoader(Glide.with(this)),
                object: SearchAdapterConfig.SearchCallback {
                    override fun onLiked(movie: Movie) {
                        dispatch?.invoke(LikeMovie(movie, !movie.liked))
                    }

                    override fun onClicked(movie: Movie, sharedElement: Pair<View, String>?) {
                        path?.getRouter()?.let {
                            dispatch?.invoke(Navigation(it, MoviePageFragment.MoviePath(movie, sharedElement)))
                        }
                    }

                    override fun onLoadMore() {
                        dispatch?.invoke(SearchAction.LoadMore)
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
                            dispatch?.invoke(SearchAction.Search(it))
                        }
                )

        subscribe(d)
        searchView?.addTextChangedListener(watcher)
        this.querySubject = subject

        unsubscribe = MovieRatingsApplication.store.subscribe { state, dispatch ->  render(state.searchPage, dispatch)}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribe?.invoke()
        this.dispatch = null
    }

    private fun render(state: SearchPageState, dispatch: Dispatch) {
        this.dispatch = dispatch
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
                setData(state.movies, state.progress is Progress.Success.Loaded)
                appInfoContainer?.bottomSlide(500)
            }

            is Progress.Paginating -> adapterConfig?.showLoadingMore(true)
        }
    }

    private fun clearData() {
        showLoading(false)
        appInfoContainer?.slideUp(500)
        clearButton?.visibility = View.GONE
        adapter?.data = ArrayList()
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            recyclerView?.visibility = View.GONE
        }
    }

    private fun setData(movies: List<Movie>, isFirstPage: Boolean) {
        showLoading(false)
        adapter?.data = movies
        adapter?.notifyDataSetChanged()
        recyclerView?.visibility = View.VISIBLE
        recyclerView?.post {
            when(isFirstPage) {
                true -> recyclerView?.scrollToPosition(0)
                false -> adapterConfig?.showLoadingMore(false)
            }
        }
    }

    private fun clearQuery() {
        dispatch?.invoke(SearchAction.Clear)
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
//                    presenter?.retrySearch()
                })
    }

    private fun createExtraLayoutHelper(): (() -> SearchItemViewHolder.ExtraLayoutHelper)? {
        return when(path) {
            is SearchPath2.AddToCollection -> {
                return ::createAddToCollectionExtraLayout
            }

            else -> null
        }
    }

    private fun createAddToCollectionExtraLayout(): SearchItemViewHolder.ExtraLayoutHelper {
        return AddToCollectionMovieLayoutHelper(object : AddToCollectionMovieLayoutHelper.Callback {
            override fun onAddRequested(movie: Movie) {
                /*presenter?.takeIf { it is SearchPresenter.AddToCollectionPresenter }
                        ?.let { it as SearchPresenter.AddToCollectionPresenter }?.addToCollection(movie)*/
            }
        })
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.search_page_title
    }

    sealed class SearchPath2: RouterPath<SearchPageFragment2>() {

        class Default(private val preferences: UserPreferences): SearchPageFragment2.SearchPath2() {
            override fun createFragmentInstance(): SearchPageFragment2 {
                return SearchPageFragment2()
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

        class AddToCollection(val collection: MovieCollection): SearchPageFragment2.SearchPath2() {
            override fun createFragmentInstance(): SearchPageFragment2 {
                return SearchPageFragment2()
            }

            override fun showBackButton() = true
        }
    }

}