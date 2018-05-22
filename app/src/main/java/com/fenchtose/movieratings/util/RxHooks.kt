package com.fenchtose.movieratings.util

import io.reactivex.Scheduler

interface RxHooks {
    fun ioThread(): Scheduler
    fun mainThread(): Scheduler
}