package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.util.Constants
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MovieProviderModule(context: Context, private val dao: MovieDao,
                          private val analytics: AnalyticsDispatcher?) {

    private val gson = GsonBuilder().setDateFormat("dd MM yyyy").create()
    private val app: MovieRatingsApplication = context.applicationContext as MovieRatingsApplication

    val movieProvider by lazy {
        if (Constants.USE_DUMMY_API) preloadedMovieProvider else retrofitProvider
    }

    private val retrofitProvider: RetrofitMovieProvider by lazy {
        val retrofit = Retrofit.Builder()
                .client(app.getOkHttpClient())
                .baseUrl(Constants.OMDB_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        RetrofitMovieProvider(retrofit, dao, analytics)
    }

    private val preloadedMovieProvider: PreloadedMovieProvider by lazy {
        PreloadedMovieProvider(app, dao)
    }

    fun release() {

    }
}