package com.fenchtose.movieratings.features.likes_page

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.search_page.SearchPageAdapter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.DbFavoriteMovieProvider
import com.fenchtose.movieratings.model.image.GlideLoader

class LikesPageFragment: BaseFragment(), LikesPage {

    override fun getScreenTitle() = R.string.likes_page_title

    private var recyclerView: RecyclerView? = null
    private var adapter: SearchPageAdapter? = null

    private var presenter: LikesPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = MovieRatingsApplication.getDatabase().movieDao()
        val favoriteProvider = DbFavoriteMovieProvider(dao)
        presenter = LikesPresenter(favoriteProvider)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.likes_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview) as RecyclerView

        val adapter = SearchPageAdapter(context, GlideLoader(Glide.with(this)),
                object : SearchPageAdapter.AdapterCallback {
                    override fun onLiked(movie: Movie) {
//                        presenter?.setLiked(movie)
                    }
                })

        adapter.setHasStableIds(true)

        recyclerView?.let {
            it.adapter = adapter
            it.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            it.visibility = View.GONE
        }

        this.adapter = adapter

        presenter?.attachView(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
    }

    override fun setData(movies: ArrayList<Movie>) {
        recyclerView?.visibility = View.VISIBLE
        adapter?.setData(movies)
        adapter?.notifyDataSetChanged()
    }


    override fun canGoBack() = true

    class LikesPath : RouterPath<LikesPageFragment>() {
        override fun createFragmentInstance(): LikesPageFragment {
            return LikesPageFragment()
        }
    }
}