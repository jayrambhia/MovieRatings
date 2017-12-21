package com.fenchtose.movieratings.base

import android.os.Bundle
import android.support.v4.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseFragment : Fragment(), FragmentNavigation {

    private var disposables: CompositeDisposable? = null
    var path: RouterPath<out BaseFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables?.dispose()
    }

    fun subscribe(d: Disposable) {
        disposables?.add(d)
    }

}