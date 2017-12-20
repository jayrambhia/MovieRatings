package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie

class ToastUtils {
    companion object {
        @SuppressLint("WrongConstant", "InflateParams")
        fun showFlutterToast(context: Context, message: String, durationInMs: Int): Toast {
            val inflater = LayoutInflater.from(context)
            val root = inflater.inflate(R.layout.floating_toast_layout, null)
            val toast = Toast(context)

            toast.view = root
            root.findViewById<TextView>(R.id.rating_view).text = message
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
            return toast
        }

        fun showMovieRating(context: Context, movie: Movie, durationInMs: Int = 2000): Toast {
            return showFlutterToast(context, context.resources.getString(R.string.floating_rating_content,
                    "${movie.title}: ${movie.ratings[0].value}"), durationInMs)
        }
    }
}