package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.gsonadapters.IntAdapter
import com.fenchtose.movieratings.util.Constants
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MovieProviderModule(val app: MovieRatingsApplication) {

    private val gson = GsonBuilder()
            .setDateFormat("dd MM yyyy")
            .registerTypeAdapter(Int::class.java, IntAdapter())
            .create()

    private val dao = MovieRatingsApplication.database.movieDao()

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

        RetrofitMovieProvider(retrofit, dao)
    }

    private val preloadedMovieProvider: PreloadedMovieProvider by lazy {
        PreloadedMovieProvider(app, dao)
    }
}