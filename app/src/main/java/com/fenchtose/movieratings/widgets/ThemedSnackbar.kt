package com.fenchtose.movieratings.widgets

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.fenchtose.movieratings.R

class ThemedSnackbar {
    companion object {
        fun make(view: View, @StringRes resId: Int, duration: Int): Snackbar {
            return Snackbar.make(view, resId, duration)
        }

        fun makeWithAction(view: View, @StringRes resId: Int, duration: Int, @StringRes actionResId: Int, listener: View.OnClickListener): Snackbar {
            return Snackbar.make(view, resId, duration)
                    .setAction(actionResId, listener)
                    .setActionTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
        }

        fun makeWithAction(view: View, content: String, duration: Int, @StringRes actionResId: Int, listener: View.OnClickListener): Snackbar {
            return Snackbar.make(view, content, duration)
                    .setAction(actionResId, listener)
                    .setActionTextColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
        }
    }
}