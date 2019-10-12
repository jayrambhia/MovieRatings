package com.fenchtose.movieratings.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.fenchtose.movieratings.R

class IndicatorTabLayout: HorizontalScrollView {

    private val strip: SlidingStrip = SlidingStrip(context)
    private val tabs: MutableList<Tab> = ArrayList()
    private val listeners: ArrayList<((Int) -> Unit)> = ArrayList()

    private var current: Int = -1

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        isHorizontalScrollBarEnabled = false
        addView(strip, 0, LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    fun addTab(tab: Tab) {
        val total = tabs.size
        tabs.add(tab)
        strip.addView(tab.view, strip.createLayoutParams())
        tab.view.setOnClickListener {
            selectTab(total)
        }

        /*if (current == -1) {
            selectTab(0)
        }*/
    }

    fun selectTab(position: Int) {
        current = position
        strip.onTabSelected(position)
        listeners.forEach { it(position) }
    }

    fun addListener(listener: (Int)->Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Int)->Unit) {
        listeners.remove(listener)
    }

    fun getCurrentTab(): Int {
        return current
    }

    class Tab(val view: View)

    class SlidingStrip(context: Context) : LinearLayout(context) {

        private var selected: Int = -1

        private var indicatorLeft: Float = 0f
        private var indicatorRight: Float = 0f

        private val indicatorHeight = context.resources.getDimension(R.dimen.indicator_height)
        private val indicatorWidth = context.resources.getDimension(R.dimen.indicator_width)
        private val indicatorBottomMargin = context.resources.getDimension(R.dimen.indicator_bottom_margin)

        private val paint: Paint
        private val rect: RectF = RectF(0f, 0f, 0f, 0f)

        private val TAG = "sliding strip"

        init {
            orientation = HORIZONTAL
            setWillNotDraw(false)
            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = ContextCompat.getColor(context, R.color.onyx_accent)
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val width = MeasureSpec.getSize(widthMeasureSpec)
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec)

            val equalWidth = MeasureSpec.makeMeasureSpec(width/childCount, MeasureSpec.EXACTLY)
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.measure(equalWidth, heightMeasureSpec)
            }

            if (selected != -1 && (indicatorLeft <= 0 || indicatorRight <= 0)) {
                // Add pre draw listener because views might not have been laid out yet
                getChildAt(selected)?.apply {
                    viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            updateIndicatorPosition(getIndicatorLeft(this@apply), getIndicatorRight(this@apply))
                            viewTreeObserver.removeOnPreDrawListener(this)
                            return true
                        }
                    })
                }
            }
        }

        fun onTabSelected(position: Int) {
            if (selected == -1) {
                getChildAt(position)?.apply {
                    updateIndicatorPosition(getIndicatorLeft(this), getIndicatorRight(this))
                }
            } else {
                animateIndicatorToPosition(position, 300)
            }

            selected = position

            for(i in 0 until childCount) {
                getChildAt(i).isSelected = i == position
            }

            invalidate()
        }

        private fun getIndicatorLeft(view: View): Float {
            return view.x + view.measuredWidth/2 - indicatorWidth/2
        }

        private fun getIndicatorRight(view: View): Float {
            return getIndicatorLeft(view) + indicatorWidth
        }

        private fun animateIndicatorToPosition(position: Int, duration: Int) {
            val child = getChildAt(position)

            val startLeft = indicatorLeft
            val startRight = indicatorRight

            val targetLeft = getIndicatorLeft(child)
            val targetRight = getIndicatorRight(child)

            if (targetLeft <= 0 || targetRight <= 0) {
                invalidate()
                return
            }

            val animator = ValueAnimator()
            animator.interpolator = FastOutLinearInInterpolator()
            animator.duration = duration.toLong()
            animator.setFloatValues(0f, 1f)
            animator.addUpdateListener {
                val fraction = it.animatedValue as Float
                updateIndicatorPosition(lerp(startLeft, targetLeft, fraction), lerp(startRight, targetRight, fraction))
            }

            animator.start()
        }

        private fun updateIndicatorPosition(left: Float, right: Float) {
            indicatorLeft = left
            indicatorRight = right
            ViewCompat.postInvalidateOnAnimation(this)
        }

        private fun lerp(start: Float, end: Float, fraction: Float): Float {
            return start + (fraction * (end - start))
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (indicatorLeft >= 0 && indicatorRight > indicatorLeft) {
                rect.left = indicatorLeft
                rect.right = indicatorRight
                rect.top = height - indicatorHeight - indicatorBottomMargin
                rect.bottom = height.toFloat() - indicatorBottomMargin
                canvas.drawRoundRect(rect, 4f, 4f, paint)
            }
        }

        fun createLayoutParams(): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }
    }
}


