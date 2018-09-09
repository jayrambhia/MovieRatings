package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.db.movieRatings.DbMovieRatingStore
import com.fenchtose.movieratings.util.Constants
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

class MovieRatingProviderModule(val app: MovieRatingsApplication, val gson: Gson) {
    private val dao = MovieRatingsApplication.database.movieRatingsDao()

    val ratingProvider by lazy {

        val flutterApi: Retrofit? = if (BuildConfig.RATINGS_ENDPOINT.isNotEmpty()) flutterApi() else null
        val omdbApi: Retrofit? = if (BuildConfig.OMDB_API_KEY.isNotEmpty()) omdbApi() else null

        if (flutterApi != null || omdbApi == null) {
            RetrofitMovieRatingsProvider(flutterApi, omdbApi, dao,
                    DbMovieRatingStore.getInstance(dao))
        } else {
            PreloadedRatingsProvider(app)
        }
    }

    private fun omdbApi(): Retrofit {
        return Retrofit.Builder()
                .client(app.getOkHttpClient())
                .baseUrl(Constants.OMDB_ENDPOINT)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    private fun flutterApi(): Retrofit {
        val cache = Cache(File(app.cacheDir, "http-cache"), 10 * 1024 * 1024)
        return Retrofit.Builder()
                .client(app.getOkHttpClient(cache, listOf(RatingHeaderInterceptor())))
                .baseUrl(BuildConfig.RATINGS_ENDPOINT)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    class RatingHeaderInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().addHeader("app-version", BuildConfig.VERSION_NAME)
                    .addHeader("api-key", BuildConfig.RATINGS_API_KEY)
                    .build()

            return chain.proceed(request)
        }

    }
}