package com.fenchtose.movieratings.features.moviepage

import android.widget.Toast
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.router.ResultBus
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.RecentlyBrowsed
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MoviePresenter(private val provider: MovieProvider,
                     private val likeStore: LikeStore,
                     private val recentStore: RecentlyBrowsedStore,
                     private val collectionStore: MovieCollectionStore,
                     private val imdbId: String?,
                     private val movie: Movie?): Presenter<MoviePage>() {

    private var loadedMovie: Movie? = null
    private var selectedCollection: MovieCollection? = null

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun attachView(view: MoviePage) {
        super.attachView(view)
        imdbId?.let {
            loadMovie(it)
        }

        val d = ResultBus.subscribe(CollectionListPageFragment.CollectionListPagePath.SELECTED_COLLECTION)
                .subscribe {
                    ResultBus.clearResult(CollectionListPageFragment.CollectionListPagePath.SELECTED_COLLECTION)
                    it.getResult().takeIf { it is MovieCollection }?.let {
                        selectedCollection = it as MovieCollection
                        loadedMovie?.let {
                            addedToCollection(selectedCollection!!, it)
                        }
                    }
                }

        subscribe(d)
    }

    private fun loadMovie(imdbId: String) {
        movie?.let {
            @Suppress("UselessCallOnNotNull")
            if (it.isComplete()) {
                showMovie(movie)
                return
            } else if (!it.poster.isNullOrEmpty()) {
                getView()?.loadImage(it.poster)
            }
        }

        val d = provider
                .getMovieWithImdb(imdbId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showMovie(it)
                }, {
                    loadedMovie = null
                    it.printStackTrace()
                    getView()?.showError()
                })

        subscribe(d)
    }

    private fun showMovie(movie: Movie) {
        loadedMovie = movie
        getView()?.showMovie(movie)
        updateRecent(movie.imdbId)
        selectedCollection?.let {
            addedToCollection(it, movie)
        }
    }

    private fun updateRecent(imdbId: String) {
        val recent = RecentlyBrowsed()
        recent.id = imdbId
        recent.timestamp = System.currentTimeMillis()
        recentStore.update(recent)
    }

    private fun addedToCollection(collection: MovieCollection, movie: Movie) {
        val d = collectionStore.isMovieAddedToCollection(collection, movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it) {
                        showAlreadyAddedToCollection(collection)
                    }
                }.filter {
                    !it
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    collectionStore.addMovieToCollection(collection, movie)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showAddedToCollection(collection)
                }, {
                    it.printStackTrace()
                    showUnableToAddToCollection(collection)
                })
        subscribe(d)
    }

    private fun showAlreadyAddedToCollection(collection: MovieCollection) {
        Toast.makeText(MovieRatingsApplication.instance, "already exists in collection: ${collection.name}", Toast.LENGTH_SHORT).show()
        selectedCollection = null
    }

    private fun showAddedToCollection(collection: MovieCollection) {
        Toast.makeText(MovieRatingsApplication.instance, "added to collection: ${collection.name}", Toast.LENGTH_SHORT).show()
        selectedCollection = null
    }

    private fun showUnableToAddToCollection(collection: MovieCollection) {
        Toast.makeText(MovieRatingsApplication.instance, "unable to add to collection ${collection.name}", Toast.LENGTH_SHORT).show()
        selectedCollection = null
    }

    fun likeToggle(): Boolean {
        loadedMovie?.let {
            it.liked = !it.liked
            likeStore.setLiked(it.imdbId, it.liked)
            return it.liked
        }

        return false
    }

    fun addToCollection() {
        MovieRatingsApplication.router?.go(CollectionListPageFragment.CollectionListPagePath(true))
    }
}