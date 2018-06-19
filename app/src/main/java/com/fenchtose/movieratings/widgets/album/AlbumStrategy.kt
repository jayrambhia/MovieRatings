package com.fenchtose.movieratings.widgets.album

interface AlbumStrategy {
    fun solve(puzzles: List<AlbumPuzzle>, width: Int, height: Int): List<AlbumPiece>
}