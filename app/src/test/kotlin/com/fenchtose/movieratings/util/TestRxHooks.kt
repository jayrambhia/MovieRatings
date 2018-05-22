package com.fenchtose.movieratings.util

import io.reactivex.schedulers.Schedulers

class TestRxHooks: RxHooks {
    override fun ioThread() = Schedulers.trampoline()
    override fun mainThread() = Schedulers.trampoline()
}