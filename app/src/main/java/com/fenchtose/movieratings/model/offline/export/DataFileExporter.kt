package com.fenchtose.movieratings.model.offline.export

import android.net.Uri
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.MovieDb
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movie.DbMovieStore
import com.fenchtose.movieratings.model.db.movie.MovieStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.DbRecentlyBrowsedStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedStore
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FileUtils
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class DataFileExporter(
        private val movieStore: MovieStore,
        private val likeStore: LikeStore,
        private val collectionStore: MovieCollectionStore,
        private val recentlyBrowsedStore: RecentlyBrowsedStore) : DataExporter<Uri> {

    private val TAG = "DataFileExported"

    companion object {
        fun newInstance(db: MovieDb): DataFileExporter {
            return DataFileExporter(
                    DbMovieStore.getInstance(db.movieDao()),
                    DbLikeStore.getInstance(db.favDao()),
                    DbMovieCollectionStore.getInstance(db.movieCollectionDao()),
                    DbRecentlyBrowsedStore.getInstance(db.recentlyBrowsedDao()))
        }
    }

    private var resultPublisher: PublishSubject<DataExporter.Progress<Uri>>? = null

    override fun observe(): Observable<DataExporter.Progress<Uri>> {
        if (resultPublisher == null || resultPublisher?.hasComplete() == true) {
            resultPublisher = PublishSubject.create()
        }

        return resultPublisher!!
    }

    override fun release() {
        resultPublisher?.onComplete()
        resultPublisher = null
    }

    override fun export(output: Uri, config: DataExporter.Config) {
        Single.defer {
            val json = JsonObject()
            json.addProperty(Constants.EXPORT_APP, Constants.EXPORT_APP_NAME)
            json.addProperty(Constants.EXPORT_VERSION, BuildConfig.VERSION_NAME)
            Single.just(Pair(HashSet<String>(), json))
        }.flatMap {
            pair ->
            if (config.favs) {
                likeStore.export()
                        .map {
                            if (it.isNotEmpty()) {
                                pair.second.add(Constants.EXPORT_LIKES, MovieRatingsApplication.gson.toJsonTree(it))
                                it.map { it.id }.toCollection(pair.first)
                            }
                            pair
                        }

            } else {
                Single.just(pair)
            }
        }.flatMap {
            pair ->
            if (config.collections) {
                collectionStore.export()
                        .map {
                            if (it.isNotEmpty()) {
                                pair.second.add(Constants.EXPORT_COLLECTIONS, MovieRatingsApplication.gson.toJsonTree(it))
                                it.map { it.entries }.flatten().map { it.movieId }.toCollection(pair.first)
                            }
                            pair
                        }

            } else {
                Single.just(pair)
            }
        }.flatMap {
            pair ->
            if (config.recentlyBrowsed) {
                recentlyBrowsedStore.export()
                        .map {
                            if (it.isNotEmpty()) {
                                it.map { it.id }.toCollection(pair.first)
                                pair.second.add(Constants.EXPORT_RECENTLY_BROWSED, MovieRatingsApplication.gson.toJsonTree(it))
                            }
                            pair
                        }
            } else {
                Single.just(pair)
            }
        }.flatMap {
            pair -> movieStore.export(pair.first)
                .map {
                    if (it.isNotEmpty()) {
                        pair.second.add(Constants.EXPORT_MOVIES, MovieRatingsApplication.gson.toJsonTree(it))
                    }

                    pair.second
                }
        }.flatMap {
            json -> Single.defer {
                Single.fromCallable {
                    FileUtils.export(MovieRatingsApplication.instance!!, output, MovieRatingsApplication.gson.toJson(json))
                }
            }
        }.subscribeOn(Schedulers.io())
        .subscribe ({
            success -> resultPublisher?.onNext(if (success) DataExporter.Progress.Success(output) else DataExporter.Progress.Error())
        }, {
            error -> resultPublisher?.onNext(DataExporter.Progress.Error())
            error.printStackTrace()
        })

        resultPublisher?.onNext(DataExporter.Progress.Started())

    }

    override fun exportCollection(output: Uri, collection: MovieCollection) {
        Single.defer {
            val json = JsonObject()
            json.addProperty(Constants.EXPORT_APP, Constants.EXPORT_APP_NAME)
            json.addProperty(Constants.EXPORT_VERSION, BuildConfig.VERSION_NAME)
            Single.just(Pair(HashSet<String>(), json))
        }.flatMap {
            pair ->
                collectionStore.export(collection.id)
                        .map {
                            if (it.isNotEmpty()) {
                                pair.second.add(Constants.EXPORT_COLLECTIONS, MovieRatingsApplication.gson.toJsonTree(it))
                                it.map { it.entries }.flatten().map { it.movieId }.toCollection(pair.first)
                            }
                            pair
                        }

        }.flatMap {
            pair -> movieStore.export(pair.first)
                .map {
                    if (it.isNotEmpty()) {
                        pair.second.add(Constants.EXPORT_MOVIES, MovieRatingsApplication.gson.toJsonTree(it))
                    }

                    pair.second
                }
        }.flatMap {
            json -> Single.defer {
            Single.fromCallable {
                FileUtils.export(MovieRatingsApplication.instance!!, output, MovieRatingsApplication.gson.toJson(json))
            }
        }
        }.subscribeOn(Schedulers.io())
                .subscribe ({
                    success -> resultPublisher?.onNext(if (success) DataExporter.Progress.Success(output) else DataExporter.Progress.Error())
                }, {
                    error -> resultPublisher?.onNext(DataExporter.Progress.Error())
                    error.printStackTrace()
                })

        resultPublisher?.onNext(DataExporter.Progress.Started())

    }
}