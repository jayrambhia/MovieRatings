package com.fenchtose.movieratings.features.moviepage

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader

class MoviePageFragment: BaseFragment(), MoviePage {

    var movie: Movie? = null

    private val posterView by bind<ImageView>(R.id.poster_view)
    private val ratingView by bind<TextView>(R.id.rating_view)
    private val titleView by bind<TextView>(R.id.title_view)
    private val genreView by bind<TextView>(R.id.genre_view)
    private val directorView by bind<TextView>(R.id.director_view)
    private val releasedView by bind<TextView>(R.id.released_view)
    private val actorsHeader by bind<TextView>(R.id.actors_header)
    private val actorsView by bind<TextView>(R.id.actors_view)
    private val writersHeader by bind<TextView>(R.id.writers_header)
    private val writersView by bind<TextView>(R.id.writers_view)

    private val plotHeader by bind<LinearLayout>(R.id.plot_header)
    private val plotToggle by bind<ImageView>(R.id.plot_toggle)
    private val plotView by bind<TextView>(R.id.plot_view)

    private val fab by bind<FloatingActionButton>(R.id.fab)

    private var isTransitionPostponeStarted = false
    private var isPosterLoaded = false

    private val actorSection by lazy {
        TextSection(actorsHeader, actorsView)
    }

    private val writerSection by lazy {
        TextSection(writersHeader, writersView)
    }

    private val plotSection by lazy {
        ExpandableSection(plotHeader, plotToggle, plotView)
    }

    private var presenter: MoviePresenter? = null

    private val imageLoader: ImageLoader by lazy {
        GlideLoader(Glide.with(this))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        presenter = MoviePresenter(MovieRatingsApplication.movieProviderModule.movieProvider,
                DbLikeStore(MovieRatingsApplication.database.favDao()),
                movie?.imdbId, movie)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presenter?.attachView(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
    }

    override fun showMovie(movie: Movie) {
        titleView.text = movie.title
        genreView.text = movie.genre
        if (movie.ratings.size > 0) {
            setRating(movie.ratings[0].value)
        } else {
            ratingView.visibility = View.GONE
        }

        directorView.text = getString(R.string.movie_page_direct_by, movie.director)
        releasedView.text = getString(R.string.movie_page_released_on, movie.released)
        actorSection.setContent(movie.actors)
        writerSection.setContent(movie.writers)
        plotSection.setContent(movie.plot)

        if (!isPosterLoaded) {
            loadImage(movie.poster)
        }

        setLiked(movie.liked)

        fab.setOnClickListener {
            val isLiked = presenter?.likeToggle()
            setLiked(isLiked)
        }
    }

    override fun loadImage(poster: String) {
        val handler = Handler()

        path?.getSharedTransitionElement()?.let {
            ViewCompat.setTransitionName(posterView, it.second)
        }

        imageLoader.loadImage(poster, posterView, object : ImageLoader.Callback {
            override fun imageLoaded(image: String, view: ImageView) {
                isPosterLoaded = true
                handler.postDelayed({

                    if (!isTransitionPostponeStarted) {
                        startPostponedEnterTransition()
                        isTransitionPostponeStarted = true
                    }

                    val params = posterView.layoutParams as CoordinatorLayout.LayoutParams
                    params.behavior = PosterBehavior()
                    posterView.layoutParams = params

                } , 60)

            }
        })
    }

    override fun showLoading() {
    }

    override fun showError() {
        showSnackbar(R.string.movie_page_load_error)
    }

    private fun setLiked(isLiked: Boolean?) {
        isLiked?.let {
            fab.setImageResource(if (it) R.drawable.ic_favorite_onyx_24dp else R.drawable.ic_favorite_border_onyx_24dp)
        }
    }

    private fun setRating(score: String) {
        val text = SpannableString(score)
        text.setSpan(RelativeSizeSpan(2f), 0, score.indexOfFirst { it == '/' }, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        ratingView.text = text

        val params = ratingView.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = RatingBehavior(context)
        ratingView.layoutParams = params
        ratingView.visibility = View.VISIBLE
    }

    override fun canGoBack(): Boolean {
        return true
    }

    override fun getScreenTitle(): Int {
        return R.string.movie_page_title
    }

    class MoviePath(private val movie: Movie, private val sharedElement: Pair<View, String>?): RouterPath<MoviePageFragment>() {

        override fun createFragmentInstance(): MoviePageFragment {
            val fragment = MoviePageFragment()
            fragment.movie = movie
            return fragment
        }

        override fun getSharedTransitionElement(): Pair<View, String>? {
            return sharedElement
        }
    }

    class TextSection(header: View?, contentView: TextView) : PageSection<TextView>(header, contentView) {
        override fun setContent(content: String?) {
            if (content.isNullOrBlank()) {
                header?.visibility = View.GONE
                contentView.visibility = View.GONE
            } else {
                header?.visibility = View.VISIBLE
                contentView.visibility = View.VISIBLE
                contentView.text = content
            }
        }
    }

    class ExpandableSection(header: View?, private val toggleButton: View, contentView: TextView) : PageSection<TextView>(header, contentView) {
        private var isExpanded = false

        override fun setContent(content: String?) {
            contentView.visibility = View.GONE
            if (content.isNullOrBlank()) {
                header?.visibility = View.GONE
                return
            }

            contentView.text = content
            isExpanded = false

            val listener = View.OnClickListener {
                contentView.visibility = if (isExpanded) View.GONE else View.VISIBLE
                toggleButton.rotation = if (isExpanded) 0f else 180f
                isExpanded = !isExpanded
            }

            toggleButton.setOnClickListener(listener)
            header?.setOnClickListener(listener)
        }

    }

    abstract class PageSection<out T: View>(val header: View?, val contentView: T) {
        abstract fun setContent(content: String?)
    }
}

