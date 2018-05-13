package com.fenchtose.movieratings.model.offline.import

import android.content.Context
import android.net.Uri
import android.support.annotation.WorkerThread
import android.util.Log
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.Fav
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movie.MovieStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FileUtils
import com.fenchtose.movieratings.util.fromJson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class DataFileImporter(private val context: Context,
                       private val likeStore: LikeStore,
                       private val collectionStore: MovieCollectionStore,
                       private val movieStore: MovieStore): DataImporter {

    private val TAG = "DataFileImporter"

    private val IMPORT_SUCCESS = "success"
    private val IMPORT_UNKNOWN_FILE = "Flutter could not recognize this file"
    private val IMPORT_UNSUPPORTED_VERSION = "Current version of the app does not support this file"
    private val IMPORT_UNKNOWN_ERROR = "The app encountered an unknown error while importing data"

    private var resultPublisher: PublishSubject<DataImporter.Progress>? = null

    override fun observe(): Observable<DataImporter.Progress> {
        if (resultPublisher == null || resultPublisher?.hasComplete() == true) {
            resultPublisher = PublishSubject.create()
        }

        return resultPublisher!!
    }

    override fun import(uri: Uri) {
        Observable.defer {
            Observable.fromCallable {
                FileUtils.readUri(context, uri)
            }
        }
                .flatMap {
                    try {
                        val json = MovieRatingsApplication.gson.fromJson<JsonElement>(it, JsonElement::class.java) as JsonObject
                        Observable.just(json)
                    } catch (e: MalformedJsonException) {
                        Observable.error<JsonObject>(e)
                    }
                }
                .map {
                    saveData(it)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it == IMPORT_SUCCESS) {
                        resultPublisher?.onNext(DataImporter.Progress.Success())
                    } else {
                        resultPublisher?.onNext(DataImporter.Progress.Error(it))
                    }
                }, {
                    it.printStackTrace()
                    if (it is JsonSyntaxException) {
                        resultPublisher?.onNext(DataImporter.Progress.Error(IMPORT_UNKNOWN_FILE))
                    } else {
                        resultPublisher?.onNext(DataImporter.Progress.Error(IMPORT_UNKNOWN_ERROR))
                    }
                })

        resultPublisher?.onNext(DataImporter.Progress.Started())
    }

    override fun release() {
        resultPublisher?.onComplete()
        resultPublisher = null
    }

    @WorkerThread
    private fun saveData(data: JsonObject): String {
        val name = data.get(Constants.EXPORT_APP)?.asString
        val version = data.get(Constants.EXPORT_VERSION)?.asString

        if (name == null || name != Constants.EXPORT_APP_NAME) {
            return IMPORT_UNKNOWN_FILE
        }

        if (version == null) {
            return IMPORT_UNSUPPORTED_VERSION
        }

        data.get(Constants.EXPORT_LIKES)?.asJsonArray?.run {
            val favs = MovieRatingsApplication.gson.fromJson<List<Fav>>(this)
            val entries = likeStore.import(favs)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "favs: total ${favs.size}, entries added: $entries")
            }
        }

        data.get(Constants.EXPORT_COLLECTIONS)?.asJsonArray?.run {
            val collections = MovieRatingsApplication.gson.fromJson<List<MovieCollection>>(this)
            val entries = collectionStore.import(collections)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "collection entries: total ${collections.sumBy { it.entries.size }}, entries added: $entries")
            }
        }

        data.get(Constants.EXPORT_MOVIES)?.asJsonArray?.run {
            val movies = MovieRatingsApplication.gson.fromJson<List<Movie>>(this)
            val entries = movieStore.import(movies)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "movies: total ${movies.size}, entries added: $entries")
            }
        }

        return IMPORT_SUCCESS
    }
}