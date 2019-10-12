package com.fenchtose.movieratings.features.settings.bubble

import androidx.annotation.ColorInt
import com.fenchtose.movieratings.features.stickyview.BubbleSize

data class BubbleColorEvent(@ColorInt val color: Int)
data class BubbleDetailEvent(val size: BubbleSize)