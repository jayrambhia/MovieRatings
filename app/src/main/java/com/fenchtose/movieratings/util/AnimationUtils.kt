package com.fenchtose.movieratings.util

import android.animation.Animator
import android.view.View

fun View.bottomSlide(duration: Long) {

    if (this.visibility != View.VISIBLE) {
        return
    }

    this.clearAnimation()
    this.animate()
            .translationY(this.height.toFloat())
            .setDuration(duration)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                }

            }).start()
}

fun View.slideUp(duration: Long) {

    if (this.visibility == View.VISIBLE) {
        return
    }

    this.visibility = View.VISIBLE
    this.clearAnimation()
    this.animate()
            .translationY(0f)
            .setDuration(duration)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

            }).start()
}