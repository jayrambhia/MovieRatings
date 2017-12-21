package com.fenchtose.movieratings.features.moviepage

import android.os.Build
import android.support.annotation.RequiresApi
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class DetailTransition: TransitionSet() {
    init {
        ordering = ORDERING_TOGETHER
        addTransition(ChangeTransform())
        addTransition(ChangeImageTransform())
    }
}