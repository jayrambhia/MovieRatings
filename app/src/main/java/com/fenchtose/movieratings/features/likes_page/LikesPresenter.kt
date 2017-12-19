package com.fenchtose.movieratings.features.likes_page

import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.api.provider.FavoriteMovieProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class LikesPresenter(private val provider: FavoriteMovieProvider) : Presenter<LikesPage>() {

    override fun attachView(view: LikesPage) {
        super.attachView(view)
        loadData()
    }

    private fun loadData() {
        val d = provider.getMovies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            getView()?.setData(it)
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )

        subscribe(d)
    }
}