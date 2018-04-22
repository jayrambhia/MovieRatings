package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPageFragment
import com.fenchtose.movieratings.features.tts.Speaker
import com.fenchtose.movieratings.model.Episode
import com.fenchtose.movieratings.model.EpisodesList
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.widgets.pagesection.*

class MoviePageFragment: BaseFragment(), MoviePage {

    var movie: Movie? = null

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

    private var presenter: MoviePresenter? = null

    private var imageLoader: ImageLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        presenter = MoviePresenter(MovieRatingsApplication.movieProviderModule.movieProvider,
                DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                DbRecentlyBrowsedStore(MovieRatingsApplication.database.recentlyBrowsedDao()),
                DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                SettingsPreferences(context),
                movie?.imdbId, movie)

        presenter?.restoreState(path?.restoreState())

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

        collectionsFlexView = MoviePageFlexView(context, view.findViewById(R.id.collections_flexview),
                object : MoviePageFlexView.CollectionCallback {
                    override fun onItemClicked(collection: MovieCollection) {
                        MovieRatingsApplication.router?.go(CollectionPageFragment.CollectionPagePath(collection))
                    }

                    override fun onAddToCollectionClicked() {
                        presenter?.addToCollection()
                    }
        })


        fab = view.findViewById(R.id.fab)

        genreSection = SimpleTextSection(view.findViewById(R.id.genre_view))
        directorSection = InlineTextSection(view.findViewById(R.id.director_view), R.string.movie_page_direct_by)
        releaseSection = InlineTextSection(view.findViewById(R.id.released_view), R.string.movie_page_released_on)
        actorSection = TextSection(view.findViewById(R.id.actors_header), view.findViewById(R.id.actors_view))
        writerSection = TextSection(view.findViewById(R.id.writers_header), view.findViewById(R.id.writers_view))
        plotSection = ExpandableSection(view.findViewById(R.id.plot_header), view.findViewById(R.id.plot_toggle), view.findViewById(R.id.plot_view))

        episodesSection = EpisodesSection(context, view.findViewById<TextView>(R.id.episodes_header),
                view.findViewById(R.id.episodes_recyclerview), view.findViewById(R.id.seasons_spinner), presenter)

        isPosterLoaded = false

        presenter?.attachView(this)
    }

    override fun saveState(): PresenterState? {
        return presenter?.saveState()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            R.id.action_open_imdb -> presenter?.openImdb(context)
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        MovieRatingsApplication.refWatcher?.watch(this)
    }

    private fun showMovie(movie: Movie) {
        MovieRatingsApplication.router?.updateTitle(movie.title)
        titleView?.text = movie.title
        genreSection?.setContent(movie.genre)
        if (movie.ratings.size > 0) {
            setRating(movie.ratings[0].value)
        } else {
            ratingView?.visibility = View.GONE
        }

        directorSection?.setContent(" ${movie.director}")
        releaseSection?.setContent(" ${movie.released}")
        actorSection?.setContent(movie.actors)
        writerSection?.setContent(movie.writers)
        plotSection?.setContent(movie.plot)

        collectionsFlexView?.setCollections(movie.collections)

        if (!isPosterLoaded) {
            loadImage(movie.poster)
        }

        setLiked(movie.liked)

        fab?.setOnClickListener {
            val isLiked = presenter?.likeToggle()
            setLiked(isLiked)
        }

        val preferences = SettingsPreferences(context)
        if (preferences.isSettingEnabled(UserPreferences.USE_TTS) && preferences.isSettingEnabled(UserPreferences.TTS_AVAILABLE)) {
            Speaker(context).talk(movie)
        }
    }

    private fun loadImage(poster: String) {
        posterView?.let {
            val handler = Handler()

            path?.getSharedTransitionElement()?.let {
                ViewCompat.setTransitionName(posterView, it.second)
            }

            imageLoader?.loadImage(poster, it, object : ImageLoader.Callback {
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

    }

    override fun updateState(state: MoviePage.State) {
        when(state) {
            is MoviePage.State.Loading -> return
            is MoviePage.State.Success -> showMovie(state.movie)
            is MoviePage.State.LoadImage -> loadImage(state.image)
            is MoviePage.State.Error -> showError()
        }
    }

    override fun updateState(state: MoviePage.CollectionState) {
        val resId = when(state) {
            is MoviePage.CollectionState.Exists -> R.string.movie_collection_movie_exists
            is MoviePage.CollectionState.Added -> R.string.movie_collection_movie_added
            is MoviePage.CollectionState.Error -> R.string.movie_collection_movie_error
        }

        showSnackbar(context.getString(resId, state.collection.name))
    }

    override fun updateState(state: MoviePage.EpisodeState) {
        episodesSection?.setContent(state)
    }

    private fun showError() {
        showSnackbar(R.string.movie_page_load_error)
    }

    private fun setLiked(isLiked: Boolean?) {
        isLiked?.let {
            fab?.setImageResource(if (it) R.drawable.ic_favorite_onyx_24dp else R.drawable.ic_favorite_border_onyx_24dp)
        }
    }

    private fun setRating(score: String) {
        val text = SpannableString(score)
        if (score.indexOfFirst { it == '/' } != -1) {
            text.setSpan(RelativeSizeSpan(2f), 0, score.indexOfFirst { it == '/' }, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        } else {
            text.setSpan(RelativeSizeSpan(2f), 0, score.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
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

        override fun showMenuIcons(): IntArray {
            return intArrayOf(R.id.action_open_imdb)
        }

    }

    class EpisodesSection(private val context: Context, private val header: View, private val recyclerView: RecyclerView,
                          private val spinner: Spinner, private val seasonSelector: SeasonSelector?): PageSection<MoviePage.EpisodeState> {

        private var adapter: EpisodesAdapter? = null
        private var spinnerAdapter: SpinnerAdapter? = null

        override fun setContent(state: MoviePage.EpisodeState) {
            when(state) {
                is MoviePage.EpisodeState.Success -> showEpisodes(state.season)
                is MoviePage.EpisodeState.Invalid -> setVisibility(View.GONE)
                else -> {

                }
            }
        }

        private fun showEpisodes(season: EpisodesList) {
            setupSpinner(season.totalSeasons, season.season)
            setVisibility(View.VISIBLE)
            val adapter = getAdapter()
            adapter.updateEpisodes(season.episodes)
            adapter.notifyDataSetChanged()
            return
        }

        private fun setVisibility(visible: Int) {
            header.visibility = visible
            recyclerView.visibility = visible
            spinner.visibility = visible
        }

        private fun getAdapter(): EpisodesAdapter {
            if (this.adapter == null) {
                val adapter = EpisodesAdapter(context, object : EpisodesAdapter.Callback {
                    override fun onSelected(episode: Episode) {
                        seasonSelector?.openEpisode(episode)
                    }
                })
                adapter.setHasStableIds(true)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.layoutManager.isAutoMeasureEnabled = true
                recyclerView.isNestedScrollingEnabled = false
                recyclerView.adapter = adapter
                this.adapter = adapter
            }

            return this.adapter!!
        }

        private fun setupSpinner(total: Int, current: Int) {
            if (spinnerAdapter == null) {
                val seasons = ArrayList<String>()
                for (season in 1..total) {
                    seasons.add(context.getString(R.string.movie_page_seasons_title, season))
                }
                val adapter = ArrayAdapter<String>(context, R.layout.spinner_selection_season, seasons)
                adapter.setDropDownViewResource(R.layout.spinner_selection_season_item)
                spinner.adapter = adapter
                spinner.setSelection(current - 1)
                this.spinnerAdapter = adapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        seasonSelector?.selectSeason(position + 1)
                    }
                }
            }
        }

    }

}

