package com.fenchtose.movieratings.features.season.episode

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.model.entity.Episode
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.widgets.ThemedSnackbar
import com.fenchtose.movieratings.widgets.pagesection.ExpandableSection
import com.fenchtose.movieratings.widgets.pagesection.InlineTextSection
import com.fenchtose.movieratings.widgets.pagesection.SimpleTextSection
import com.fenchtose.movieratings.widgets.pagesection.TextSection

@SuppressLint("ViewConstructor")
class EpisodePage(context: Context, private val episode: Episode,
                  private val series: Movie): FrameLayout(context) {

    private val progressbar: ProgressBar
    private val content: ViewGroup

    private val titleSection: SimpleTextSection
    private val seriesTitleSection: SimpleTextSection
    private val ratingSection: SimpleTextSection
    private val genreSection: SimpleTextSection
    private val directorSection: InlineTextSection
    private val releaseSection: InlineTextSection
    private val actorSection: TextSection
    private val writerSection: TextSection
    private val plotSection: ExpandableSection

    var callback: EpisodeCallback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.episode_page_layout, this, true)
        progressbar = findViewById(R.id.progressbar)
        content = findViewById(R.id.episode_content)
        titleSection = SimpleTextSection(findViewById(R.id.title_view))
        seriesTitleSection = SimpleTextSection(findViewById(R.id.series_view))
        ratingSection = SimpleTextSection(findViewById(R.id.rating_view), R.string.episode_page_rating)
        genreSection = SimpleTextSection(findViewById(R.id.genre_view))
        directorSection = InlineTextSection(findViewById(R.id.director_view), R.string.episode_page_directed_by)
        releaseSection = InlineTextSection(findViewById(R.id.released_view), R.string.episode_page_released_on)
        actorSection = TextSection(findViewById(R.id.actors_header), findViewById(R.id.actors_view))
        writerSection = TextSection(findViewById(R.id.writers_header), findViewById(R.id.writers_view))
        plotSection = ExpandableSection(findViewById(R.id.plot_header),
                findViewById(R.id.plot_toggle),
                findViewById(R.id.plot_view),
                GaEvents.EXPAND_PLOT.withCategory(category()),
                GaEvents.COLLAPSE_PLOT.withCategory(category()))
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
    }

    private var presenter: EpisodePresenter? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter = EpisodePresenter(MovieRatingsApplication.movieProviderModule.movieProvider, episode)
        presenter?.attachView(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter?.detachView(this)
        presenter = null
    }

    fun updateState(state: State) {
        when(state) {
            is State.Loading -> showLoading()
            is State.Success -> showEpisode(state.episode)
            is State.Error -> showError()
        }
    }

    private fun showLoading() {
        progressbar.visibility = View.VISIBLE
        content.visibility = View.GONE
    }

    private fun showError() {
        progressbar.visibility = View.GONE
        ThemedSnackbar.makeWithAction(this,
                R.string.episode_page_loading_error,
                Snackbar.LENGTH_LONG,
                R.string.episode_page_retry_cta,
                View.OnClickListener {
                    presenter?.reload()
                }
        ).show()
    }

    private fun showEpisode(episode: Movie) {

        callback?.onEpisodeLoaded(episode)

        progressbar.visibility = View.GONE
        content.visibility = View.VISIBLE

        titleSection.setContent(episode.title)
        seriesTitleSection.setContent(context.getString(R.string.episode_page_series_title, series.title, this.episode.season))
        ratingSection.setContent(episode.ratings.firstOrNull()?.rating)
        genreSection.setContent(episode.genre)
        directorSection.setContent(" ${episode.director}")
        releaseSection.setContent(" ${episode.released}")
        actorSection.setContent(episode.actors)
        writerSection.setContent(episode.writers)
        plotSection.setContent(episode.plot)

    }

    private fun category() = GaCategory.EPISODE

    sealed class State {
        object Loading: State()
        class Success(val episode: Movie): State()
        object Error: State()
    }

    interface EpisodeCallback {
        fun onEpisodeLoaded(episode: Movie)
    }
}