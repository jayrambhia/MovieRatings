package com.fenchtose.movieratings.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StyleRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v4.widget.TextViewCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.features.stickyview.BubbleSize
import com.fenchtose.movieratings.util.isColorDark

@SuppressLint("ViewConstructor")
class RatingBubble(context: Context, color: Int, size: BubbleSize) : FrameLayout(context) {

    @ColorInt
    private var color: Int? = null

    private val label: TextView
    private val closeButton: ImageView
    private val closeButtonRight: ImageView
    private var size = BubbleSize.SMALL
    private var left = true

    init {
        LayoutInflater.from(context).inflate(R.layout.floating_rating_view, this, true)
        label = findViewById(R.id.rating_view)
        closeButton = findViewById(R.id.close_btn)
        closeButtonRight = findViewById(R.id.close_btn_right)
        this.color = color
        this.size = size
        setup()
    }

    private fun setup() {
        setBackgroundResource(getBubbleBackground(false, size))
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        color?.let {
            updateColor(it)
        }
    }

    fun updateDirection(left: Boolean) {
        this.left = left
        val params = label.layoutParams as FrameLayout.LayoutParams
        setBackgroundResource(getBubbleBackground(left, size))

        if (left) {
            closeButtonRight.visibility = View.VISIBLE
            closeButton.visibility = View.GONE
            if (params.leftMargin != 0) {
                params.rightMargin = params.leftMargin
            }
            params.leftMargin = 0
        } else {
            closeButtonRight.visibility = View.GONE
            closeButton.visibility = View.VISIBLE
            if (params.rightMargin != 0) {
                params.leftMargin = params.rightMargin
            }
            params.rightMargin = 0
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
        TextViewCompat.setTextAppearance(label, textStyle)

        @ColorRes val imageColor = if (isDark) R.color.textColorLight else R.color.textColorDark
        ImageViewCompat.setImageTintList(closeButton, ColorStateList.valueOf(ContextCompat.getColor(context, imageColor)))
        ImageViewCompat.setImageTintList(closeButtonRight, ColorStateList.valueOf(ContextCompat.getColor(context, imageColor)))
    }

    fun updateSize(size: BubbleSize) {
        if (this.size == size) {
            return
        }

        this.size = size
        updateDirection(left)
    }

    private fun getBubbleBackground(left: Boolean, size: BubbleSize): Int {
        return when(size) {
            BubbleSize.SMALL -> if (left) R.drawable.floating_rating_view_background_left else R.drawable.floating_rating_view_background
            BubbleSize.BIG -> if (left) R.drawable.floating_rating_view_big_background_left else R.drawable.floating_rating_view_big_background
        }
    }

    fun setText(text: CharSequence) {
        label.text = text
    }

    fun isClickForClose(x: Int): Boolean {
        val button = if (closeButtonRight.visibility == View.VISIBLE) closeButtonRight else closeButton
        return x > button.x && x < button.x + button.width
    }
}