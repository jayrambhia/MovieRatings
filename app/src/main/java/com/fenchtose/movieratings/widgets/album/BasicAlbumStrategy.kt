package com.fenchtose.movieratings.widgets.album


class BasicAlbumStrategy: AlbumStrategy {

    override fun solve(puzzles: List<AlbumPuzzle>, width: Int, height: Int): List<AlbumPiece> {
        val pieces = ArrayList<AlbumPiece>()
        when {
            puzzles.size == 1 -> pieces.add(fit(puzzles[0], 0, 0, width, height))
            puzzles.size %2 == 1 -> {
                val sectionHeight = height/(puzzles.size/2 + 1)
                pieces.addAll(solve(puzzles.subList(0, puzzles.size - 1), width, height - sectionHeight))
                pieces.add(fit(puzzles[puzzles.size - 1], 0, height - sectionHeight, width, sectionHeight))
            }
            puzzles.size % 2 == 0 -> {
                var index = 0
                var top = 0
                val steps = puzzles.size/2
                while(index < puzzles.size - 1) {
                    pieces.add(fit(puzzles[index], 0, top, width/2, height/steps))
                    pieces.add(fit(puzzles[index + 1], width/2, top, width/2, height/steps))
                    index += 2
                    top += height/steps
                }
            }
        }

        return pieces
    }

    private fun fit(puzzle: AlbumPuzzle, left: Int, top:Int, width: Int, height: Int): AlbumPiece {
        val src = AlbumRect(0, 0, puzzle.width, puzzle.height)
        val wRatio = puzzle.width/width.toFloat()
        val hRatio = puzzle.height/height.toFloat()

        if (wRatio >= 1 && hRatio >= 1) {
            // just crop
            src.left = (puzzle.width - width)/2
            src.right = src.left + width
            src.top = (puzzle.height - height)/2
            src.bottom = src.top + height
        } else if (wRatio >= 1 || wRatio >= hRatio) {
            // fit height, crop width
            val scaledWidth = (puzzle.width / hRatio).toInt()
            val l = ((scaledWidth - width)/2)*hRatio
            src.left = l.toInt()
            src.right = src.left + (width*hRatio).toInt()
        } else if (hRatio >= 1 || wRatio < hRatio) {
            // fit width, crop height
            val scaledHeight = (puzzle.height / wRatio).toInt()

            // but we don't actually scale. canvas will do it for us.
            val t = ((scaledHeight - height)/2)*wRatio

            src.top = t.toInt()
            src.bottom = src.top + (height*wRatio).toInt()
        }

        return AlbumPiece(puzzle, src, AlbumRect(left, top, left+width, top+height))
    }
}