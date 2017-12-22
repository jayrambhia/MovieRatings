package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.util.Constants
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MovieProviderModule(val app: MovieRatingsApplication) {

    val gson = GsonBuilder().setDateFormat("dd MM yyyy").create()
    val dao = app.getDatabase().movieDao()

    companion object {
        var instance: MovieProviderModule? = null
    }

    init {
        instance = this
    }

    private val retrofitProvider: RetrofitMovieProvider by lazy {
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.OMDB_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitMovieProvider(retrofit, dao)
    }

    private val preloadedMovieProvider: PreloadedMovieProvider by lazy {
        PreloadedMovieProvider(app, dao)
    }

    fun getMovieProvider(): MovieProvider {
        return if (Constants.USE_DUMMY_API) preloadedMovieProvider else retrofitProvider
    }
}