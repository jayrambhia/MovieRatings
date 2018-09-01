package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import com.fenchtose.movieratings.model.db.entity.MovieRating
import com.fenchtose.movieratings.model.entity.Trending
import io.reactivex.Observable

class PreloadedRatingsProvider(context: Context): MovieRatingsProvider {

    override fun report404(title: String, year: String?) {
        throw RuntimeException("PreloadedRatingProvider should not be used in release")
    }

    override fun useFlutterApi(status: Boolean) {
        throw RuntimeException("PreloadedRatingProvider should not be used in release")
    }

    override fun getMovieRating(request: RatingRequest): Observable<MovieRating> {
        throw RuntimeException("PreloadedRatingProvider should not be used in release")
    }

    override fun getTrending(period: String): Observable<Trending> {
        throw RuntimeException("PreloadedRatingProvider should not be used in release")
    }

}