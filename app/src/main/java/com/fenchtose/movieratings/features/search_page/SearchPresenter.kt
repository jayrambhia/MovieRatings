package com.fenchtose.movieratings.features.search_page

import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SearchPresenter(private val provider: MovieProvider) : Presenter<SearchPage>() {

    fun onSearchRequested(query: String) {

        getView()?.showLoading(true)

        val d = provider.search(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            if (it.success) {
                                getView()?.setData(it.results)
                            }
                        },
                        onError = {
                            it.printStackTrace()
                        }
                )

        subscribe(d)
    }
}