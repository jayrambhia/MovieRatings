package com.fenchtose.movieratings.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.fenchtose.movieratings.R

class ToastUtils {
    companion object {
        fun showFlutterToast(context: Context, message: String) {
            val inflater = LayoutInflater.from(context)
            val root = inflater.inflate(R.layout.floating_toast_layout, null)
            val toast = Toast(context)

            toast.view = root
            (root.findViewById(R.id.rating_view) as TextView)?.text = message
            toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.duration = 3000
            toast.show()
        }
    }
}