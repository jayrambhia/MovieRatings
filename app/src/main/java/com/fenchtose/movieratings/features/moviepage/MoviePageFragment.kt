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
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader

class MoviePageFragment: BaseFragment(), MoviePage {

    var movie: Movie? = null

    private var posterView: ImageView? = null
    private var ratingView: TextView? = null
    private var titleView: TextView? = null
    private var genreView: TextView? = null
    private var directorView: TextView? = null
    private var releasedView:TextView? = null
    private var actorsHeader:TextView? = null
    private var actorsView:TextView? = null
    private var writersHeader:TextView? = null
    private var writersView:TextView? = null

    private var plotHeader:LinearLayout? = null
    private var plotToggle:ImageView? = null
    private var plotView:TextView? = null

    private var fab: FloatingActionButton? = null

    private var isTransitionPostponeStarted = false
    private var isPosterLoaded = false

    private var actorSection: TextSection? = null
    private var writerSection: TextSection? = null
    private var plotSection: ExpandableSection? = null

    private var presenter: MoviePresenter? = null

    private val imageLoader: ImageLoader by lazy {
        GlideLoader(Glide.with(this))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        presenter = MoviePresenter(MovieRatingsApplication.movieProviderModule.movieProvider,
                DbLikeStore(MovieRatingsApplication.database.favDao()),
                DbRecentlyBrowsedStore(MovieRatingsApplication.database.recentlyBrowsedDao()),
                movie?.imdbId, movie)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        posterView = view.findViewById(R.id.poster_view)
        ratingView = view.findViewById(R.id.rating_view)
        titleView = view.findViewById(R.id.title_view)
        genreView = view.findViewById(R.id.genre_view)
        directorView = view.findViewById(R.id.director_view)
        releasedView = view.findViewById(R.id.released_view)
        actorsHeader = view.findViewById(R.id.actors_header)
        actorsView = view.findViewById(R.id.actors_view)
        writersHeader = view.findViewById(R.id.writers_header)
        writersView = view.findViewById(R.id.writers_view)

        plotHeader = view.findViewById(R.id.plot_header)
        plotToggle = view.findViewById(R.id.plot_toggle)
        plotView = view.findViewById(R.id.plot_view)

        fab = view.findViewById(R.id.fab)

        actorSection = TextSection(actorsHeader, actorsView!!)
        writerSection = TextSection(writersHeader, writersView!!)
        plotSection = ExpandableSection(plotHeader, plotToggle!!, plotView!!)


        presenter?.attachView(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
    }

    override fun showMovie(movie: Movie) {
        titleView?.text = movie.title
        genreView?.text = movie.genre
        if (movie.ratings.size > 0) {
            setRating(movie.ratings[0].value)
        } else {
            ratingView?.visibility = View.GONE
        }

        directorView?.text = getString(R.string.movie_page_direct_by, movie.director)
        releasedView?.text = getString(R.string.movie_page_released_on, movie.released)
        actorSection?.setContent(movie.actors)
        writerSection?.setContent(movie.writers)
        plotSection?.setContent(movie.plot)

        if (!isPosterLoaded) {
            loadImage(movie.poster)
        }

        setLiked(movie.liked)

        fab?.setOnClickListener {
            val isLiked = presenter?.likeToggle()
            setLiked(isLiked)
        }
    }

    override fun loadImage(poster: String) {
        if (posterView == null) {
            return
        }

        val handler = Handler()

        path?.getSharedTransitionElement()?.let {
            ViewCompat.setTransitionName(posterView, it.second)
        }

        imageLoader.loadImage(poster, posterView!!, object : ImageLoader.Callback {
            override fun imageLoaded(image: String, view: ImageView) {
                isPosterLoaded = true
                handler.postDelayed({

                    if (!isTransitionPostponeStarted) {
                        startPostponedEnterTransition()
                        isTransitionPostponeStarted = true
                    }

                    val params = posterView?.layoutParams as CoordinatorLayout.LayoutParams
                    params.behavior = PosterBehavior()
                    posterView?.layoutParams = params

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
            fab?.setImageResource(if (it) R.drawable.ic_favorite_onyx_24dp else R.drawable.ic_favorite_border_onyx_24dp)
        }
    }

    private fun setRating(score: String) {
        val text = SpannableString(score)
        text.setSpan(RelativeSizeSpan(2f), 0, score.indexOfFirst { it == '/' }, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        ratingView?.text = text

        val params = ratingView?.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = RatingBehavior(context)
        ratingView?.layoutParams = params
        ratingView?.visibility = View.VISIBLE
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

