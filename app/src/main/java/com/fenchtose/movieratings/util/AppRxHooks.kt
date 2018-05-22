package com.fenchtose.movieratings.util

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AppRxHooks: RxHooks {
    override fun ioThread() = Schedulers.io()
    override fun mainThread() = AndroidSchedulers.mainThread()

}