package com.fenchtose.movieratings.display

import android.content.Context
import android.view.*
import com.fenchtose.movieratings.widgets.RatingBubble
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class BubbleTouchListener(manager: WindowManager, context: Context, rightPosition: Int, callback: BubbleCallback): View.OnTouchListener {

    private val gestureListener: GestureListener = GestureListener(manager, rightPosition, callback)
    private val gestureDetector: GestureDetector = GestureDetector(context, gestureListener)


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    fun attach(view: RatingBubble) {
        view.setOnTouchListener(this)
        gestureListener.attach(view)
    }

    fun release() {
        gestureListener.release()
    }

    class GestureListener(private val manager: WindowManager,
                          private val rightPosition: Int,
                          private val callback: BubbleCallback): GestureDetector.SimpleOnGestureListener() {

        private var bubble: RatingBubble? = null
        private var publisher: PublishSubject<Pair<Int, Boolean>>? = null
        private var disposable: Disposable? = null

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (velocityX < -1000) {
                updateSide(true)
            } else if (velocityX > 1000) {
                updateSide(false)
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            updateY(e2.rawY.toInt())
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            callback.onClick(bubble, e.x.toInt(), e.y.toInt())
            return super.onSingleTapUp(e)
        }

        private fun updateY(yDest: Int) {
            bubble?.let {
                val params = it.layoutParams
                if (params is WindowManager.LayoutParams) {
                    params.y = yDest
                    manager.updateViewLayout(it, params)
                    publisher?.onNext(Pair(params.y, params.x == 0))
                }
            }
        }

        private fun updateSide(left: Boolean) {
            bubble?.let {
                val params = it.layoutParams
                if (params is WindowManager.LayoutParams) {
                    if (left) {
                        params.x = 0
                    } else {
                        params.x = rightPosition
                    }
                    manager.updateViewLayout(it, params)
                    publisher?.onNext(Pair(params.y, params.x == 0))
                }

                it.updateDirection(left)
            }
        }

        fun attach(view: RatingBubble) {
            this.bubble = view
            release()
            publisher = PublishSubject.create()
            disposable = publisher?.let {
                it.debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ callback.updatePosition(bubble, it.first, it.second) }, {})
            }
        }

        fun release() {
            publisher?.onComplete()
        }
    }

    interface BubbleCallback {
        fun onClick(bubble: RatingBubble?, x: Int, y: Int)
        fun updatePosition(bubble: RatingBubble?, y: Int, left: Boolean)
    }
}
