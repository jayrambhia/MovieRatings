package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import android.support.annotation.RawRes
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.db.entity.MovieRating
import com.fenchtose.movieratings.model.entity.Trending
import com.google.gson.Gson
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.io.IOException

class PreloadedRatingsProvider(private val context: Context): MovieRatingsProvider {

    override fun getMovieRating(request: RatingRequest): Observable<MovieRating> {
        val observable = when(request.title.toLowerCase()) {
            "friends" -> Observable.just(convertToRating(R.raw.friends_rating))
            "thor" -> Observable.just(convertToRating(R.raw.thor_rating))
            "batman begins" -> Observable.just(convertToRating(R.raw.batman_begins_rating))
            else -> Observable.error(Throwable("PreloadedRatingProvider does not support this search: $request.title"))
        }

        return Observable.defer {
            observable
        }
    }

    private fun convertToRating(@RawRes resId: Int): MovieRating {
        val data = readRawFile(resId)
        val gson = Gson()
        return gson.fromJson(data, MovieRating::class.java)
    }

    private fun convertToTrending(@RawRes resId: Int): Trending {
        val data = readRawFile(resId)
        val gson = Gson()
        return gson.fromJson(data,  Trending::class.java)
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


    override fun useFlutterApi(status: Boolean) {
        // TODO nothing
    }

    override fun report404(title: String, year: String?) {
        // TODO nothing
    }

    override fun getTrending(period: String): Observable<Trending> {
        return Observable.defer {
            Observable.just(convertToTrending(R.raw.trending))
        }
    }
}