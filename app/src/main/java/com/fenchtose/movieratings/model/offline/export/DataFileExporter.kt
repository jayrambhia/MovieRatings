package com.fenchtose.movieratings.model.offline.export

import android.util.Log
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movie.MovieStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedStore
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FileUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class DataFileExporter(
        private val movieStore: MovieStore,
        private val likeStore: LikeStore,
        private val collectionStore: MovieCollectionStore,
        private val recentlyBrowsedStore: RecentlyBrowsedStore) : DataExporter<String> {

    private val TAG = "DataFileExported"

    private var resultPublisher: PublishSubject<DataExporter.Progress<String>>? = null

    override fun observe(): Observable<DataExporter.Progress<String>> {
        if (resultPublisher == null || resultPublisher?.hasComplete() == true) {
            resultPublisher = PublishSubject.create()
        }

        return resultPublisher!!
    }

    override fun release() {
        resultPublisher?.onComplete()
        resultPublisher = null
    }

    override fun export(config: DataExporter.Config) {
        Log.d(TAG, "export called")

        Observable.zip(likeStore.export(), collectionStore.export(),
                BiFunction<JsonArray, JsonArray, JsonObject> { likes, collections ->
                    val json = JsonObject()
                    json.add(Constants.EXPORT_LIKES, likes)
                    json.add(Constants.EXPORT_COLLECTIONS, collections)
                    json
                })
                .flatMap {
                    json ->
                        if (config.includeHistory) {
                            recentlyBrowsedStore.export()
                                    .doOnNext{json.add(Constants.EXPORT_RECENTLY_BROWSED, it)}
                                    .map { json }
                        } else {
                            Observable.just(json)
                        }
                }
                .flatMap {
                    json -> movieStore.export()
                        .doOnNext { json.add(Constants.EXPORT_MOVIES, it) }.map { json }
                }
                .doOnNext {
                    it.addProperty(Constants.EXPORT_APP, Constants.EXPORT_APP_NAME)
                    it.addProperty(Constants.EXPORT_VERSION, BuildConfig.VERSION_NAME)
                }
                .flatMap {
                    json -> Observable.defer {
                        Observable.fromCallable {
                            FileUtils.export(MovieRatingsApplication.instance!!, MovieRatingsApplication.gson.toJson(json))?: ""
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    filename -> resultPublisher?.onNext(if (!filename.isEmpty()) DataExporter.Progress.Success(filename) else DataExporter.Progress.Error())
                }, {
                    error -> resultPublisher?.onNext(DataExporter.Progress.Error())
                    error.printStackTrace()
                })

        resultPublisher?.onNext(DataExporter.Progress.Started())

    }
}