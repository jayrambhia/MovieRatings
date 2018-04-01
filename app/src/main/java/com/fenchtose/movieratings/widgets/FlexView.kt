package com.fenchtose.movieratings.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fenchtose.movieratings.R

class FlexView : ViewGroup {

    private val TAG = "FlexView"

    private var verticalSpacing = 0
    private var horizontalSpacing = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setup(context, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.FlexView)
            a?.let {
                verticalSpacing = a.getDimensionPixelOffset(R.styleable.FlexView_flex__vertical_spacing, verticalSpacing)
                horizontalSpacing = a.getDimensionPixelOffset(R.styleable.FlexView_flex__horizontal_spacing, horizontalSpacing)
                a.recycle()
            }
        }

        if (isInEditMode) {
            val inflater = LayoutInflater.from(context)
            for (i in 0 .. 1) {
                val textview = inflater.inflate(R.layout.movie_page_collection_item_layout, this, false) as TextView
                @SuppressLint("SetTextI18n")
                textview.text = "Flex item view $i"
                addElement(textview)
            }

            addElement(inflater.inflate(R.layout.movie_page_add_to_collection_layout, this, false))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (childCount == 0) {
            return
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight

        var totalHeight = 0

        // Add first child
        val child0 = getChildAt(0)
        measureChild(child0, widthMeasureSpec, heightMeasureSpec)
        var nrows = 1
        var rowHeight = child0.measuredHeight
        var rowWidth = paddingLeft + child0.measuredWidth

        for (i in 1 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            if (exceedsRow(rowWidth, childWidth, width)) {
                // Create new row and add self there.
                nrows++
                rowWidth = paddingLeft + childWidth
                // Add previous row's height to total height
                totalHeight += rowHeight
                rowHeight = childHeight
            } else {
                // Add self to the left one
                rowWidth += horizontalSpacing + childWidth
                rowHeight = Math.max(childHeight, rowHeight)
            }
        }

        // Add final row's height to total height
        totalHeight += rowHeight

        // Add padding and vertical spacings
        totalHeight += paddingTop + paddingBottom + verticalSpacing * Math.max(0, nrows-1)

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.UNSPECIFIED))
        }
    }

    private fun exceedsRow(currentRowWidth: Int, childWidth: Int, availableWidth: Int): Boolean {
        if (currentRowWidth + childWidth + horizontalSpacing > availableWidth) {
            // it exceeds. Check if there are any elements in the row
            return (currentRowWidth - paddingLeft) != 0
        }

        return false
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = paddingLeft
        val right = r - paddingRight
        val width = right - left - l
        val top = paddingTop

        var totalHeight = paddingTop

        if (childCount == 0) {
            return
        }

        // Draw first child
        val child0 = getChildAt(0)
        var rowHeight = top + child0.measuredHeight
        var rowWidth = left + child0.measuredWidth
        child0.layout(left, top, rowWidth, rowHeight)

        for (i in 1 until childCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            if (exceedsRow(rowWidth, childWidth, width)) {
                // to next row
                totalHeight += rowHeight + verticalSpacing
                rowWidth = left
                rowHeight = 0

            } else {
                rowWidth += horizontalSpacing
            }

            child.layout(rowWidth, totalHeight, rowWidth + childWidth, totalHeight + childHeight)
            rowWidth += childWidth
            rowHeight = Math.max(rowHeight, childHeight)

        }
    }

    fun addElement(view: View) {
        addView(view, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    fun clearAll() {
        removeAllViews()
    }
}