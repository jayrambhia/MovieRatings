package com.fenchtose.movieratings.widgets.bottomnavigation

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import com.fenchtose.movieratings.R

class BottomNavigationBar: LinearLayout {

    private val TAG = "BottomNavigationBar"

    private val barHeight: Int
    private val listeners: ArrayList<OnItemSelected> = ArrayList()
    private val items: ArrayList<MenuItem> = ArrayList()
    private val views: ArrayList<MenuItemView> = ArrayList()

    private var current: Int = -1

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, style: Int): super(context, attrs, style) {
        barHeight = context.resources.getDimensionPixelOffset(R.dimen.bottom_bar_height)
        orientation = HORIZONTAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.bottom_bar_color))
        if (isInEditMode) {
            update(listOf(
                    MenuItem(1, R.drawable.ic_search_accent_24dp, "", ""),
                    MenuItem(2, R.drawable.ic_favorite_accent_24dp, "", ""),
                    MenuItem(3, R.drawable.ic_collections_accent_24dp, "", ""),
                    MenuItem(4, R.drawable.ic_info_outline_accent_24dp, "", "")
            ))
            updateState(0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mwidth = MeasureSpec.getSize(widthMeasureSpec)
        Log.d(TAG, "width: $mwidth")
        if (mwidth != 0) {
            val ewidth = mwidth/childCount
            Log.d(TAG, "total children: $childCount")
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                Log.d(TAG, "child width: $i: ${child.measuredWidth}, ${child.measuredHeight}")
//                measureChild(child, MeasureSpec.makeMeasureSpec(ewidth, MeasureSpec.EXACTLY), heightMeasureSpec)
            }
        }
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(barHeight, MeasureSpec.EXACTLY))
    }

    fun addListener(listener: OnItemSelected) {
        listeners.add(listener)
    }

    fun removeListener(listener: OnItemSelected) {
        listeners.remove(listener)
    }

    fun update(items: List<MenuItem>, preSelected: Int = -1) {
        clearItems()
        this.items.addAll(items)
        generateViews()
        requestLayout()
        if (preSelected != -1) {
            current = preSelected
            updateState(preSelected)
        }
    }

    private fun generateViews() {
        items.map { createView(it) }.toCollection(views)
        views.forEachIndexed {
            i, menu -> menu.setOnClickListener {
                updateState(i)
                notifyItemSelected(i, menu.item)
            }
        }
    }

    private fun createView(item: MenuItem): MenuItemView {
        val view = MenuItemView(context, item)
        val params = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT)
        params.weight = 1f
        addView(view, params)
        return view
    }

    private fun clearItems() {
        items.clear()
        views.clear()
        removeAllViews()
        current = -1
    }

    private fun updateState(position: Int) {
        views.forEachIndexed { index, view -> view.active(index == position) }
    }

    private fun notifyItemSelected(position: Int, item: MenuItem) {
        listeners.forEach { it(position, item, current == position) }
        current = position
    }
}

data class MenuItem(
        val id: Int,
        val iconRes: Int,
        val root: String = "",
        val eventLabel: String
)

typealias OnItemSelected = (position: Int, item:MenuItem, reselected: Boolean) -> Unit