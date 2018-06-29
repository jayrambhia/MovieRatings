package com.fenchtose.movieratings.features.season

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.features.season.episode.EpisodePage
import com.fenchtose.movieratings.model.EpisodesList
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.image.GlideLoader
import com.fenchtose.movieratings.model.image.ImageLoader
import com.fenchtose.movieratings.util.IntentUtils

class SeasonPageFragment: BaseFragment(), EpisodePage.EpisodeCallback {

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var adapter: EpisodePagerAdapter? = null
    private var poster: ImageView? = null
    private var imageLoader: ImageLoader? = null
    private var currentEpisode: Movie? = null

    override fun canGoBack() = true
    override fun getScreenTitle() = R.string.season_page_title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.season_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageLoader = GlideLoader(Glide.with(this))

        tabLayout = view.findViewById(R.id.tabs)
        viewPager = view.findViewById(R.id.viewpager)
        poster = view.findViewById(R.id.poster_view)

        path?.takeIf { it is SeasonPath }?.let {
            it as SeasonPath
        }?.let {
            adapter = EpisodePagerAdapter(requireContext(), it.series, it.episodes, this)
            tabLayout?.setupWithViewPager(viewPager)
            viewPager?.adapter = adapter
            viewPager?.currentItem = it.selectedEpisode - 1
            loadImage(it.series.poster)
            MovieRatingsApplication.router?.updateTitle(it.series.title)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var consumed = true
        when(item?.itemId) {
            R.id.action_open_imdb -> IntentUtils.openImdb(requireContext(), currentEpisode?.imdbId)
            else -> consumed = false
        }

        return if (consumed) true else super.onOptionsItemSelected(item)
    }

    override fun onEpisodeLoaded(episode: Movie) {
        this.currentEpisode = episode
        loadImage(episode.poster)
    }

    private fun loadImage(url: String) {
        poster?.let {
            imageView -> run {
                imageLoader?.loadImage(url, imageView)
            }
        }
    }

    class SeasonPath(val series: Movie, val episodes: EpisodesList, val selectedEpisode: Int): RouterPath<SeasonPageFragment>() {
        override fun createFragmentInstance(): SeasonPageFragment {
            return SeasonPageFragment()
        }

        override fun showMenuIcons(): IntArray {
            return intArrayOf(R.id.action_open_imdb)
        }
    }

}