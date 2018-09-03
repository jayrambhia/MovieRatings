package com.fenchtose.movieratings.util

import android.view.View

fun View.show(state: Boolean = true) {
    visibility = when(state) {
        true -> View.VISIBLE
        false -> View.GONE
    }
}