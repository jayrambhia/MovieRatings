package com.fenchtose.movieratings.util

import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.fenchtose.movieratings.R

class ToastUtils {
    companion object {
        fun showFlutterToast(context: Context, message: String, durationInMs: Int = 2000) {
            val inflater = LayoutInflater.from(context)
            val root = inflater.inflate(R.layout.floating_toast_layout, null)
            val toast = Toast(context)

            toast.view = root
            (root.findViewById(R.id.rating_view) as TextView)?.text = message
            toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, context.resources.getDimensionPixelOffset(R.dimen.toast_bottom_margin))

            toast.duration = if (durationInMs <= 10000) durationInMs else 8000

            val countDown = object: CountDownTimer(toast.duration.toLong(), 1000) {
                override fun onFinish() {
                    toast.cancel()
                }

                override fun onTick(millisUntilFinished: Long) {
                    toast.show()
                }

            }

            toast.show()
            countDown.start()
        }
    }
}