package com.fenchtose.movieratings.util

import android.view.View

fun View.show(state: Boolean = true) {
    val new = when(state) {
        true -> View.VISIBLE
        false -> View.GONE
    }

    if (new != visibility) {
        visibility = new
    }
}