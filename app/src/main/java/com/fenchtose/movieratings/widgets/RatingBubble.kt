package com.fenchtose.movieratings.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.util.isColorDark

open class RatingBubble: FrameLayout {

    @ColorInt
    private var color: Int? = null

    private val label: TextView
    private val closeButton: ImageView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.floating_rating_view, this, true)
        label = findViewById(R.id.rating_view)
        closeButton = findViewById(R.id.close_btn)
        setup(context, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        setBackgroundResource(R.drawable.floating_rating_view_background)
        color?.let {
            updateColor(it)
        }
    }

    fun updateColor(@ColorInt color: Int) {
        this.color = color

        val drawable = background
        if (drawable is GradientDrawable) {
            drawable.setColor(color)
            drawable.invalidateSelf()
        }

        val isDark = isColorDark(color)

        @StyleRes val textStyle = if (isDark) R.style.Text_Light_Medium else R.style.Text_Dark_Medium
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            label.setTextAppearance(textStyle)
        } else {
            label.setTextAppearance(context, textStyle)
        }

        @ColorRes val imageColor = if (isDark) R.color.textColorLight else R.color.textColorDark
        ImageViewCompat.setImageTintList(closeButton, ColorStateList.valueOf(ContextCompat.getColor(context, imageColor)))
    }

    fun setText(text: CharSequence) {
        label.text = text
    }
}