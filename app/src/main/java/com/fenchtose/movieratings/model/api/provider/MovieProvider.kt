package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.entity.Episode
import com.fenchtose.movieratings.model.entity.EpisodesList
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.SearchResult
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import io.reactivex.Observable

interface MovieProvider {
    fun getMovieWithImdb(imdbId: String): Observable<Movie>
    fun getMovie(title: String, year: String): Observable<Movie>
    fun search(title: String, page:Int = 1): Observable<SearchResult>
    fun getEpisodes(series: Movie, season: Int): Observable<EpisodesList>
    fun getEpisode(episode: Episode): Observable<Movie>
    fun addPreferenceApplier(applier: UserPreferenceApplier)
}