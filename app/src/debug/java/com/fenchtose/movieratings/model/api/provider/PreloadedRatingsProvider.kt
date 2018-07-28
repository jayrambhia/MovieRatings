package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import android.support.annotation.RawRes
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.MovieRating
import com.google.gson.Gson
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.io.IOException

class PreloadedRatingsProvider(private val context: Context): MovieRatingsProvider {

    override fun getMovieRating(title: String, year: String?): Observable<MovieRating> {
        val observable = when(title.toLowerCase()) {
            "friends" -> Observable.just(convertToRating(R.raw.friends_rating))
            "thor" -> Observable.just(convertToRating(R.raw.thor_rating))
            "batman begins" -> Observable.just(convertToRating(R.raw.batman_begins_rating))
            else -> Observable.error(Throwable("PreloadedRatingProvider does not support this search: $title"))
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

}