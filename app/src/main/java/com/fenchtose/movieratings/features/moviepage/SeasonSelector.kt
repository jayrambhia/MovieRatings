package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.model.Episode

interface SeasonSelector {
    fun selectSeason(season: Int)
    fun openEpisode(episode: Episode)
}