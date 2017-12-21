package com.fenchtose.movieratings.features.likespage

import android.os.Bundle
import android.support.design.widget.Snackbar
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
import com.fenchtose.movieratings.features.searchpage.SearchPageAdapter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.DbFavoriteMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.widgets.ThemedSnackbar

class LikesPageFragment: BaseFragment(), LikesPage {

    override fun getScreenTitle() = R.string.likes_page_title

    private var root: ViewGroup? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: SearchPageAdapter? = null

    private var presenter: LikesPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = MovieRatingsApplication.getDatabase().movieDao()
        val favoriteProvider = DbFavoriteMovieProvider(dao)
        val likeStore = DbLikeStore(MovieRatingsApplication.getDatabase().favDao())
        presenter = LikesPresenter(favoriteProvider, likeStore)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.likes_page_layout, container, false) as ViewGroup
        return root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)

        val adapter = SearchPageAdapter(context, GlideLoader(Glide.with(this)),
                object : SearchPageAdapter.AdapterCallback {
                    override fun onLiked(movie: Movie) {
                        presenter?.unlike(movie)
                    }

                    override fun onClicked(movie: Movie, sharedElement: View) {
                        // TODO check for api compatibility
                        presenter?.openMovie(movie, sharedElement)
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
        adapter?.data = movies
        adapter?.notifyDataSetChanged()
    }

    override fun showRemoved(movie: Movie, index: Int) {
        adapter?.notifyItemRemoved(index)
        showMovieRemoved(movie, index)
    }

    override fun showAdded(movie: Movie, index: Int) {
        adapter?.notifyItemInserted(index)
        recyclerView?.post {
            recyclerView?.scrollToPosition(index)
        }
    }

    private fun showMovieRemoved(movie: Movie, index: Int) {
        root?.let {
            ThemedSnackbar.makeWithAction(it,
                    getString(R.string.movie_unliked_snackbar_content, movie.title),
                    Snackbar.LENGTH_LONG,
                    R.string.undo_action,
                    View.OnClickListener {
                        presenter?.undoUnlike(movie, index)
                    })
                    .show()
        }
    }

    override fun canGoBack() = true

    class LikesPath : RouterPath<LikesPageFragment>() {
        override fun createFragmentInstance(): LikesPageFragment {
            return LikesPageFragment()
        }
    }
}