package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.model.entity.MovieRating

class ToastUtils {
    companion object {
        @SuppressLint("WrongConstant", "InflateParams")
        private fun showFlutterToast(context: Context, message: String, @ColorInt bubbleColor: Int): Toast {
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
            TextViewCompat.setTextAppearance(label, textStyle)

            @ColorRes val imageColor = if (isDark) R.color.textColorLight else R.color.textColorDark
            ImageViewCompat.setImageTintList(root.findViewById(R.id.logo_view), ColorStateList.valueOf(ContextCompat.getColor(context, imageColor)))

            toast.view = root
            root.findViewById<TextView>(R.id.rating_view).text = message
            toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, context.resources.getDimensionPixelOffset(R.dimen.toast_bottom_margin))
            toast.duration = Toast.LENGTH_LONG
            toast.show()
            return toast
        }

        fun showMovieRating(context: Context, rating: MovieRating, @ColorInt bubbleColor: Int): Toast {
            val title = rating.title + if (rating.translatedTitle.isNotEmpty()) " (${rating.translatedTitle})" else ""
            return showFlutterToast(context, context.resources.getString(R.string.floating_rating_content,
                    "$title: ${rating.rating}"), bubbleColor)
        }
    }
}