package com.fenchtose.movieratings.widgets.viewpager

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import com.fenchtose.movieratings.R

class MaxHeightViewPager: ViewPager {
    private val maxHeight: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (attrs != null) {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightViewPager)
            if (arr != null) {
                maxHeight = arr.getDimensionPixelOffset(R.styleable.MaxHeightViewPager_vp__max_height, -1)
                arr.recycle()
            } else {
                maxHeight = -1
            }
        } else {
            maxHeight = -1
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (maxHeight > 0 && MeasureSpec.getSize(heightMeasureSpec) > maxHeight) {
            val hSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY)
            setMeasuredDimension(widthMeasureSpec, hSpec)
            measure(widthMeasureSpec, hSpec)
        }
    }
}