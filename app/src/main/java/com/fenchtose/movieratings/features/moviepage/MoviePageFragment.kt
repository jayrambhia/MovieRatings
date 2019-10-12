package com.fenchtose.movieratings.features.moviepage

import android.os.Bundle
import android.os.Handler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.view.ViewCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPath
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPagePath
import com.fenchtose.movieratings.model.db.like.LikeMovie
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader
import com.fenchtose.movieratings.widgets.pagesection.ExpandableSection
import com.fenchtose.movieratings.widgets.pagesection.InlineTextSection
import com.fenchtose.movieratings.widgets.pagesection.SimpleTextSection
import com.fenchtose.movieratings.widgets.pagesection.TextSection
import java.lang.ref.WeakReference

class MoviePageFragment: BaseFragment() {

    private var posterView: ImageView? = null
    private var ratingView: TextView? = null
    private var titleView: TextView? = null

    private var collectionsFlexView: MoviePageFlexView? = null

    private var fab: FloatingActionButton? = null

    private var isTransitionPostponeStarted = false
    private var isPosterLoaded = false

    private var genreSection: SimpleTextSection? = null
    private var directorSection: InlineTextSection? = null
    private var releaseSection: InlineTextSection? = null
    private var actorSection: TextSection? = null
    private var writerSection: TextSection? = null
    private var plotSection: ExpandableSection? = null

    private var episodesSection: EpisodesSection? = null

    private var imageLoader: ImageLoader? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.movie_page_title
    override fun screenName() = AppScreens.MOVIE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        imageLoader = GlideLoader(Glide.with(this))
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movie_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        posterView = view.findViewById(R.id.poster_view)
        ratingView = view.findViewById(R.id.rating_view)
        titleView = view.findViewById(R.id.title_view)
        collectionsFlexView = MoviePageFlexView(requireContext(), view.findViewById(R.id.collections_flexview))

        fab = view.findViewById(R.id.fab)

        genreSection = SimpleTextSection(view.findViewById(R.id.genre_view))
        directorSection = InlineTextSection(view.findViewById(R.id.director_view), R.string.movie_page_direct_by)
        releaseSection = InlineTextSection(view.findViewById(R.id.released_view), R.string.movie_page_released_on)
        actorSection = TextSection(view.findViewById(R.id.actors_header), view.findViewById(R.id.actors_view))
        writerSection = TextSection(view.findViewById(R.id.writers_header), view.findViewById(R.id.writers_view))
        plotSection = ExpandableSection(view.findViewById(R.id.plot_header),
                view.findViewById(R.id.plot_toggle),
                view.findViewById(R.id.plot_view),
                AppEvents.togglePlot("expand", path?.category()),
                AppEvents.togglePlot("collapse", path?.category()))

        episodesSection = EpisodesSection(requireContext(), view.findViewById<TextView>(R.id.episodes_header),
                view.findViewById(R.id.episodes_recyclerview), view.findViewById(R.id.seasons_spinner), path?.getRouter())

        isPosterLoaded = false

        render { appState, dispatch ->
            if (appState.moviePages.isNotEmpty()) {
                render(appState.moviePages.last(), dispatch)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        path?.let {
            if (it is MoviePath) {
                val id = it.movie.imdbId
                dispatch?.invoke(LoadMovie(id))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var consumed = true
        when(item.itemId) {
            R.id.action_open_imdb -> {
                AppEvents.openImdb(path?.category()).track()
                dispatch?.invoke(OpenImdbPage(WeakReference(requireContext())))
            }
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    private fun render(state: MoviePageState, dispatch: Dispatch) {
        val movie = state.movie
        if (movie.imdbId.isEmpty()) {
            return
        }

        path?.getRouter()?.updateTitle(movie.title)
        titleView?.text = movie.title
        genreSection?.setContent(movie.genre)
        if (movie.ratings.isNotEmpty()) {
            setRating(movie.ratings[0].rating)
        } else {
            ratingView?.visibility = View.GONE
        }

        if (!isPosterLoaded && movie.poster.isNotEmpty()) {
            loadImage(movie.poster)
        }

        directorSection?.setContent(" ${movie.director}")
        releaseSection?.setContent(" ${movie.released}")
        actorSection?.setContent(movie.actors)
        writerSection?.setContent(movie.writers)
        plotSection?.setContent(movie.plot)

        collectionsFlexView?.render(movie.collections, { collection ->
            AppEvents.openCollection(path?.category()).track()
            path?.getRouter()?.let { dispatch.invoke(Navigation(it, CollectionPagePath(collection))) }
        }, {
            path?.getRouter()?.let {
                dispatch.invoke(Navigation(it, CollectionListPath(true, movie)))
            }
        })

        setLiked(movie.liked)

        fab?.setOnClickListener {
            AppEvents.like(path?.category(), !movie.liked).track()
            dispatch(LikeMovie(movie, !movie.liked))
        }

        episodesSection?.render(state.movie, state.season, state.seasonProgress, dispatch)
    }

    private fun setLiked(isLiked: Boolean?) {
        isLiked?.let {
            fab?.setImageResource(if (it) R.drawable.ic_favorite_onyx_24dp else R.drawable.ic_favorite_border_onyx_24dp)
        }
    }

    private fun loadImage(poster: String) {
        val posterView = posterView ?: return
        val handler = Handler()

        path?.getSharedTransitionElement()?.let {
            ViewCompat.setTransitionName(posterView, it.second)
        }

        imageLoader?.loadImage(poster, posterView, object : ImageLoader.Callback {
            override fun imageLoaded(image: String, view: ImageView) {
                isPosterLoaded = true
                handler.postDelayed({

                    if (!isTransitionPostponeStarted) {
                        startPostponedEnterTransition()
                        isTransitionPostponeStarted = true
                    }

                    val params = posterView.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
                    params.behavior = PosterBehavior()
                    posterView.layoutParams = params

                } , 60)

            }
        })
    }

    private fun setRating(score: String) {
        val text = SpannableString(score)
        if (score.indexOfFirst { it == '/' } != -1) {
            text.setSpan(RelativeSizeSpan(2f), 0, score.indexOfFirst { it == '/' }, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        } else {
            text.setSpan(RelativeSizeSpan(2f), 0, score.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        ratingView?.text = text

        val params = ratingView?.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        params.behavior = RatingBehavior(requireContext())
        ratingView?.layoutParams = params
        ratingView?.visibility = View.VISIBLE
    }
}

class MoviePath(val movie: Movie, private val sharedElement: Pair<View, String>? = null): RouterPath<MoviePageFragment>() {
    override fun createFragmentInstance() = MoviePageFragment()
    override fun showMenuIcons() = intArrayOf(R.id.action_open_imdb)
    override fun getSharedTransitionElement() = sharedElement
    override fun category() = GaCategory.MOVIE
    override fun toolbarElevation() = R.dimen.toolbar_no_elevation
    override fun initAction() = InitMoviePage(movie.imdbId, movie)
    override fun clearAction() = ClearMoviePage

    companion object {
        const val KEY = "MoviePath"
        const val MOVIE_ID = "imdb_id"

        fun createExtras(imdbId: String): Bundle {
            val bundle = Bundle()
            bundle.putString(Router.ROUTE_TO_SCREEN, MoviePath.KEY)
            bundle.putString(MoviePath.MOVIE_ID, imdbId)
            return bundle
        }

        fun createPath(): ((Bundle) -> MoviePath) {
            return {
                val movieId = it.getString(MOVIE_ID, "")
                MoviePath(Movie.withId(movieId))
            }
        }
    }
}