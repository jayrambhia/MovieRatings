package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.season.SeasonPageFragment
import com.fenchtose.movieratings.model.entity.Episode
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.Season
import com.fenchtose.movieratings.widgets.pagesection.PageSection

class EpisodesSection(private val context: Context, private val header: View, private val recyclerView: RecyclerView,
                      private val spinner: Spinner, private val router: Router?): PageSection<MoviePageState> {

    private var adapter: EpisodesAdapter? = null
    private var spinnerAdapter: SpinnerAdapter? = null
    private var dispatch: Dispatch? = null
    private var series: Movie? = null
    private var season: Season? = null

    fun render(series: Movie, season: Season?, progress: SeasonProgress, dispatch: Dispatch) {
        this.dispatch = dispatch
        this.series = series
        this.season = season
        if (progress === SeasonProgress.Loaded && season != null) {
            showEpisodes(season)
        } else if (progress == SeasonProgress.Default) {
            setVisibility(View.GONE)
        }
    }

    override fun setContent(state: MoviePageState) {
        /*when(state) {
            is MoviePage.EpisodeState.Success -> showEpisodes(state.season)
            is MoviePage.EpisodeState.Invalid -> setVisibility(View.GONE)
            else -> {

            }
        }*/
    }

    private fun showEpisodes(season: Season) {
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
                    AppEvents.OPEN_EPISODE.track()
                    router?.let {
                        val router = it
                        series?.let {
                            val series = it
                            season?.let {
                                dispatch?.invoke(Navigation(router, SeasonPageFragment.SeasonPath(series, it, episode.episode)))
                            }
                        }
                    }
                }
            })

            adapter.setHasStableIds(true)
            recyclerView.layoutManager = LinearLayoutManager(context).apply {
                isAutoMeasureEnabled = true
            }
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
                    AppEvents.SELECT_SEASON.track()
                    series?.let {
                        dispatch?.invoke(LoadSeason(it, position + 1))
                    }
                }
            }
        }
    }

}