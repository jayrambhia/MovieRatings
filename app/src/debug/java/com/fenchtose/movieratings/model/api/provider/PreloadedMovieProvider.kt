package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import android.support.annotation.RawRes
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.db.apply
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.model.entity.*
import com.google.gson.Gson
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.io.IOException


class PreloadedMovieProvider(context:Context, private val dao: MovieDao): MovieProvider {
    private val context = context.applicationContext
    private val preferenceAppliers = ArrayList<UserPreferenceApplier>()

    override fun getMovieWithImdb(imdbId: String): Observable<Movie> {
        return getMovie(imdbId, "")
    }

    override fun getMovie(title: String, year: String): Observable<Movie> {
        return getMovieFromDb(title)
                .flatMap {
                    if (it.imdbId.isNotEmpty()) {
                        Observable.just(it)
                    } else {
                        Observable.just(true)
                                .flatMap {
                                        getMovieInfo(title)
                                        .doOnNext {
                                            dao.insert(it.convert())
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
                .map {
                    it.copy(movies = it.results.map { it.convert() })
                }
                .map {
                    it.copy(movies = it.movies.map { preferenceAppliers.apply(it) })
                }
                .doOnNext {
                    it.movies.map {
                        dao.insertSearch(it.convert())
                    }
                }
    }

    override fun getEpisodes(series: Movie, season: Int): Observable<Season> {
        return Observable.defer {
            val data = readRawFile(if (season == 1) R.raw.parks_n_rec_season_1 else R.raw.parks_n_rec_season_2)
            val gson = Gson()
            Observable.just(gson.fromJson(data, Season::class.java))
        }
    }

    override fun getEpisode(episode: Episode): Observable<Movie> {
        return Observable.just(Movie.invalid())
    }

    override fun addPreferenceApplier(applier: UserPreferenceApplier) {
        preferenceAppliers.add(applier)
    }

    private fun getMovieFromDb(title: String): Observable<Movie> {
        return Observable.defer {
            val dbMovie = dao.getMovie(title)
            dbMovie?.let {
                return@defer Observable.just(dbMovie.convert())
            }

            Observable.just(Movie.invalid())
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