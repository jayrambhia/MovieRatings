package com.fenchtose.movieratings.widgets.viewpager

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet

class MaxItemHeightViewPager : androidx.viewpager.widget.ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var maxHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            maxHeight = Math.max(maxHeight, child.measuredHeight)
        }
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY))
    }
}