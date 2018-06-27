package com.fenchtose.movieratings.util

import android.graphics.Color
import android.support.annotation.ColorInt

fun isColorDark(@ColorInt color: Int): Boolean {
    return Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114 < 160
}