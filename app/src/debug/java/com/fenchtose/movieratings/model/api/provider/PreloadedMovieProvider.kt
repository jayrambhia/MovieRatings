package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import android.support.annotation.RawRes
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.EpisodesList
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.google.gson.Gson
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.io.IOException


class PreloadedMovieProvider(context:Context, private val dao: MovieDao): MovieProvider {
    private val context = context.applicationContext
    private val preferenceAppliers = ArrayList<UserPreferneceApplier>()

    override fun getMovieWithImdb(imdbId: String): Observable<Movie> {
        return getMovie(imdbId, "")
    }

    override fun getMovie(title: String, year: String): Observable<Movie> {
        return getMovieFromDb(title)
                .flatMap {
                    if (it.id != -1) {
                        Observable.just(it)
                    } else {
                        Observable.just(true)
                                .flatMap {
                                        getMovieInfo(title)
                                        .doOnNext {
                                            dao.insert(it)
                                        }
                                }

                        }
                    }
    }

    override fun search(title: String, page:Int): Observable<SearchResult> {
        val observable =  when(title) {
            "thor" -> Observable.just(convertToSearchResult(R.raw.thor))
            "batman" -> Observable.just(convertToSearchResult(R.raw.batman))
            "parks" -> Observable.just(convertToSearchResult(R.raw.park_n_rec_search))
            else -> Observable.error(Throwable("PreloadedMovieProvider does not support this search: $title"))
        }
        return observable
                .doOnNext {
                    it.results.map {
                        for (preferenceApplier in preferenceAppliers) {
                            preferenceApplier.apply(it)
                        }
                    }
                }
                .doOnNext {
                    it.results.map {
                        dao.insertSearch(it)
                    }
                }
    }

    override fun getEpisodes(series: Movie, season: Int): Observable<EpisodesList> {
        return Observable.defer {
            val data = readRawFile(if (season == 1) R.raw.parks_n_rec_season_1 else R.raw.parks_n_rec_season_2)
            val gson = Gson()
            Observable.just(gson.fromJson(data, EpisodesList::class.java))
        }
    }


    override fun addPreferenceApplier(applier: UserPreferneceApplier) {
        preferenceAppliers.add(applier)
    }

    private fun getMovieFromDb(title: String): Observable<Movie> {
        return Observable.defer {
            val movie = dao.getMovie(title)
            if (movie != null) {
                Observable.just(movie)
            } else {
                Observable.just(Movie.empty())
            }
        }
    }

    private fun getMovieInfo(title: String): Observable<Movie> {
        val data = readRawFile(if (title == "tt1266020") R.raw.parks_n_rec else R.raw.thor_ragnarok)
        val gson = Gson()
        return Observable.just(gson.fromJson(data, Movie::class.java))
    }

    private fun convertToSearchResult(@RawRes resId: Int): SearchResult {
        val data = readRawFile(resId)
        val gson = Gson()
        return gson.fromJson(data, SearchResult::class.java)
    }

    private fun readRawFile(@RawRes resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)

        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            var i = inputStream.read()
            while (i != -1) {
                byteArrayOutputStream.write(i)
                i = inputStream.read()
            }
            inputStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return byteArrayOutputStream.toString()
    }
}