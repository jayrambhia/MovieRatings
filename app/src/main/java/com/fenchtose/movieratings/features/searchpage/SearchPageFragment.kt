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
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.RetrofitMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.util.Constants
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SearchPageFragment : BaseFragment(), SearchPage {

    private var progressbar: ProgressBar? = null
    private var attributeView: TextView? = null
    private var searchView: EditText? = null
    private var clearButton: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: SearchPageAdapter? = null

    private var presenter: SearchPresenter? = null

    private var watcher: TextWatcher? = null
    private var querySubject: PublishSubject<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = GsonBuilder().setDateFormat("dd MM yyyy").create()

        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.OMDB_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        val dao = MovieRatingsApplication.getDatabase().movieDao()
//        val likeStore = PreferencesLikeStore(activity)
        val likeStore = DbLikeStore(MovieRatingsApplication.getDatabase().favDao())
        presenter = SearchPresenter(RetrofitMovieProvider(retrofit, dao), likeStore)

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

        val adapter = SearchPageAdapter(context, GlideLoader(Glide.with(this)),
                object : SearchPageAdapter.AdapterCallback {
                    override fun onLiked(movie: Movie) {
                        presenter?.setLiked(movie)
                    }
        })

        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter

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

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
        watcher?.let {
            searchView?.removeTextChangedListener(watcher)
        }
        querySubject?.onComplete()
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.search_page_title
    }

    override fun showLoading(status: Boolean) {
        if (status) {
            progressbar?.visibility = View.VISIBLE
            attributeView?.visibility = View.GONE
            recyclerView?.visibility = View.GONE
        } else {
            attributeView?.visibility = View.VISIBLE
            progressbar?.visibility = View.GONE
        }
    }

    override fun setData(movies: ArrayList<Movie>) {
        showLoading(false)
        adapter?.data = movies
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            recyclerView?.scrollToPosition(0)
        }
        recyclerView?.visibility = View.VISIBLE
    }

    private fun clearQuery() {
        clearData()
        showLoading(false)
        presenter?.onQueryCleared()
        clearButton?.visibility = View.GONE
    }

    override fun clearData() {
        adapter?.data = ArrayList()
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            recyclerView?.visibility = View.GONE
        }
    }

    class SearchPath : RouterPath<SearchPageFragment>() {
        override fun createFragmentInstance(): SearchPageFragment {
            return SearchPageFragment()
        }

        override fun showMenuIcons(): IntArray {
            return intArrayOf(R.id.action_settings, R.id.action_fav)
        }
    }
}