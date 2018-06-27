package com.fenchtose.movieratings.features.settings.bubble

import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

class BubbleDrawable(@ColorInt bubbleColor: Int): Drawable() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var selected: Boolean = false
    @ColorInt
    var color: Int = bubbleColor

    private var radius: Float = 0f
    private val checkPath: Path = Path()

    init {
        paint.style = Paint.Style.FILL
        paint.color = color

        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.strokeWidth = 8f
        outlinePaint.color = 0xfffffff
        outlinePaint.alpha = 255
        outlinePaint.strokeCap = Paint.Cap.BUTT

        maskPaint.style = Paint.Style.STROKE
        maskPaint.color = 0xff00000
        maskPaint.alpha = 120
    }

    override fun draw(canvas: Canvas) {
        val rect = bounds
        val x = rect.centerX().toFloat()
        val y = rect.centerY().toFloat()

        canvas.drawCircle(x, y, radius, paint)

        if (selected) {
            canvas.drawCircle(x, y, radius, maskPaint)
            canvas.drawCircle(x, y, radius - outlinePaint.strokeWidth, outlinePaint)
            canvas.drawPath(checkPath, outlinePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        radius = Math.min(bounds.width(), bounds.height())/2f
        checkPath.reset()

        val w = bounds.width().toFloat()
        val a = w/4.49f
        val b = 3*a
        val c = w/12f

        checkPath.moveTo(w/4, 0.75f*w - 0.87f*a - c)
        checkPath.lineTo(0.25f*w + 0.5f*a, 0.75f*w - c)
        checkPath.lineTo(0.75f*w, 0.75f*w - b/2 - c)

    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }

}