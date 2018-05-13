package com.fenchtose.movieratings.model.offline.import

import android.net.Uri
import io.reactivex.Observable

interface DataImporter {

    fun observe(): Observable<Progress>
    fun import(uri: Uri)
    fun release()

    sealed class Progress {
        class Started: Progress()
        class Error(val error: String): Progress()
        class Success: Progress()
    }
}