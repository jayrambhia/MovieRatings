package com.fenchtose.movieratings.features.season

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.features.season.episode.EpisodePage
import com.fenchtose.movieratings.model.EpisodesList
import com.fenchtose.movieratings.model.Movie

class EpisodePagerAdapter(private val context: Context,
                          private val series: Movie,
                          episodes: EpisodesList,
                          private val callback: EpisodePage.EpisodeCallback): PagerAdapter() {

    private val episodes = episodes.episodes

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = episodes.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val page = EpisodePage(context, episodes[position], series)
        page.callback = callback
        container.addView(page)
        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        (`object` as EpisodePage).callback = null
    }

    override fun getPageTitle(position: Int): CharSequence = context.getString(R.string.season_page_tab_title, position + 1)
}