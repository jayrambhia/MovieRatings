package com.fenchtose.movieratings.widgets

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import com.fenchtose.movieratings.R

class RatioImageView: AppCompatImageView {

    var ratio: Float = -1.0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup(context, attrs)
    }

    fun setup(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.RatioImageView)
            a?.let {
                ratio = a.getFloat(R.styleable.RatioImageView_ratioimageview__ratio, -1f)
                a.recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (ratio > 0) {
            val height = measuredWidth.toFloat()/ratio
            setMeasuredDimension(measuredWidth, height.toInt())
        }
    }
}