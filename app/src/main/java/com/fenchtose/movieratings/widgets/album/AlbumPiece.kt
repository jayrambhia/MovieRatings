package com.fenchtose.movieratings.widgets.album

import android.graphics.Bitmap
import android.graphics.Rect

data class AlbumPiece(val puzzle: AlbumPuzzle, val src: AlbumRect, val dst: AlbumRect)
data class AlbumRect(var left: Int, var top: Int, var right: Int, var bottom: Int) {
    fun rect(): Rect {
        return Rect(left, top, right, bottom)
    }
}
data class AlbumPuzzle(val bitmap: Bitmap, val width: Int, val height: Int)