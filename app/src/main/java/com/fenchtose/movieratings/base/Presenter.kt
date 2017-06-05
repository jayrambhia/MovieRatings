package com.fenchtose.movieratings.base

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class Presenter<VIEW> {

    private var disposables: CompositeDisposable? = null
    private var view: VIEW? = null

    @CallSuper
    fun attachView(view: VIEW) {
        disposables = CompositeDisposable()
        this.view = view
    }

    @CallSuper
    fun detachView(@Suppress("UNUSED_PARAMETER") view: VIEW) {
        disposables?.dispose()
        this.view = null
    }

    fun subscribe(d: Disposable) {
        disposables?.add(d)
    }

    fun getView(): VIEW? {
        return view
    }
}