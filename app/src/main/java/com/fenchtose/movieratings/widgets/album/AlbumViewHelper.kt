package com.fenchtose.movieratings.widgets.album

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.fenchtose.movieratings.model.image.ImageLoader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class AlbumViewHelper(private val view: ImageView, private val strategy: AlbumStrategy, private val limit: Int) {

    private val TAG = "AlbumViewHelper"

    private val imageSet = HashSet<String>()
    private val resources = ArrayList<Drawable>()
    private var isReady: Boolean = false

    private var width = 0
    private var height = 0
    private var disposable: Disposable? = null

    fun loadImages(images: List<String>, imageLoader: ImageLoader) {
        clear()

        for (image in images.filter { it != null && it.isNotEmpty() && it != "N/A" }) {
            imageSet.add(image)
            imageLoader.loadImage(image, view, object: ImageLoader.SelfLoaderCallback {
                override fun imageLoaded(image: String, view: ImageView, resource: Drawable) {
                    imageSet.remove(image)
                    resources.add(resource)
                    checkProgress()
                }

                override fun error(image: String, view: ImageView) {
                    imageSet.remove(image)
                    checkProgress()
                }
            })
        }
    }

    private fun clear() {
        imageSet.clear()
        resources.clear()
        isReady = false
        view.setImageBitmap(null)
        width = 0
        height = 0
        disposable?.dispose()
    }

    private fun checkProgress() {
        if (!isReady && (imageSet.isEmpty() || resources.size >= limit)) {
            draw()
        }
    }

    private fun draw() {
        if (resources.isEmpty()) {
            return
        }

        isReady = true

        val puzzles = resources.filter {
            it is BitmapDrawable
        }.map {
            (it as BitmapDrawable).bitmap
        }.filter {
            it != null && !it.isRecycled
        }.map {
            AlbumPuzzle(it, it.width, it.height)
        }.shuffled().take(limit)


        val width = view.measuredWidth
        val height = view.measuredHeight

        if (width == 0 || height == 0) {
            view.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    draw()
                }
            })
            return
        }

        disposable = Observable.defer {
                    Observable.just(puzzles)
                }.map {
                    strategy.solve(it, width, height)
                }.filter {
                    it.isNotEmpty()
                }.map {
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
                    val canvas = Canvas(bitmap)

                    it.forEach {
                        drawBitmap(canvas, it.puzzle.bitmap, it.src.rect(), it.dst.rect())
                    }
                    bitmap
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    bitmap -> view.setImageBitmap(bitmap)
                }, {
                    it.printStackTrace()
                })
    }

    private fun drawBitmap(canvas: Canvas, b: Bitmap, src: Rect, dest: Rect) {
        canvas.drawBitmap(b, src, dest, null)
    }
}
