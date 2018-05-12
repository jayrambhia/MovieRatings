package com.fenchtose.movieratings.model.offline.export

import android.util.Log
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movie.MovieStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedStore
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
                    json.add("likes", likes)
                    json.add("collections", collections)
                    json
                })
                .flatMap {
                    json ->
                        if (config.includeHistory) {
                            recentlyBrowsedStore.export()
                                    .doOnNext{json.add("recently_browsed", it)}
                                    .map { json }
                        } else {
                            Observable.just(json)
                        }
                }
                .flatMap {
                    json -> movieStore.export()
                        .doOnNext { json.add("movies", it) }.map { json }
                }
                .flatMap {
                    json -> Observable.defer {
                        Observable.fromCallable {
                            FileUtils.export(MovieRatingsApplication.instance!!, json.toString())?: ""
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