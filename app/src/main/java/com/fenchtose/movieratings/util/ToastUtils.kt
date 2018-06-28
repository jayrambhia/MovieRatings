package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.CountDownTimer
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.Movie

class ToastUtils {
    companion object {
        @SuppressLint("WrongConstant", "InflateParams")
        private fun showFlutterToast(context: Context, message: String, durationInMs: Int, @ColorInt bubbleColor: Int): Toast {
            val inflater = LayoutInflater.from(context)
            val root = inflater.inflate(R.layout.floating_toast_layout, null)
            val toast = Toast(context)

            val drawable = root.background
            if (drawable is GradientDrawable) {
                drawable.setColor(bubbleColor)
                drawable.invalidateSelf()
            }

            val isDark = isColorDark(bubbleColor)
            val label = root.findViewById<TextView>(R.id.rating_view)
            @StyleRes val textStyle = if (isDark) R.style.Text_Light_Medium else R.style.Text_Dark_Medium
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                label.setTextAppearance(textStyle)
            } else {
                label.setTextAppearance(context, textStyle)
            }

            @ColorRes val imageColor = if (isDark) R.color.textColorLight else R.color.textColorDark
            ImageViewCompat.setImageTintList(root.findViewById(R.id.logo_view), ColorStateList.valueOf(ContextCompat.getColor(context, imageColor)))


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

        fun showMovieRating(context: Context, movie: Movie, @ColorInt bubbleColor: Int, durationInMs: Int): Toast {
            return showFlutterToast(context, context.resources.getString(R.string.floating_rating_content,
                    "${movie.title}: ${movie.ratings[0].value}"), durationInMs, bubbleColor)
        }
    }
}