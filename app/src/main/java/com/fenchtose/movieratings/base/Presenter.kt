package com.fenchtose.movieratings.base

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class Presenter<VIEW> {

    private var disposables: CompositeDisposable? = null
    private var view: VIEW? = null

    @CallSuper
    open fun attachView(view: VIEW) {
        disposables = CompositeDisposable()
        this.view = view
    }

    @CallSuper
    open fun detachView(@Suppress("UNUSED_PARAMETER") view: VIEW) {
        disposables?.dispose()
        this.view = null
    }

    fun subscribe(d: Disposable) {
        disposables?.add(d)
    }

    fun getView(): VIEW? {
        return view
    }

    open fun saveState(): PresenterState? = null

    open fun restoreState(state: PresenterState?) {

    }
}