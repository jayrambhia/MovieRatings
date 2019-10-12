package com.fenchtose.movieratings.widgets.viewpager

import androidx.viewpager.widget.ViewPager
import android.view.View
import kotlin.math.abs

class CardsPagerTransformerShift(
        private val baseElevation: Float,
        private val raisingElevation: Float,
        private val smallerScale: Float,
        private val startOffset: Float): androidx.viewpager.widget.ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val absPosition = abs(position - startOffset)
        if (absPosition >= 1) {
            page.elevation = baseElevation
            page.scaleY = smallerScale
            page.scaleX = page.scaleY
        } else {
            // This will be during transformation
            page.elevation = ((1 - absPosition) * raisingElevation + baseElevation)
            page.scaleY = (smallerScale - 1) * absPosition + 1
            page.scaleX = page.scaleY
        }
    }

}