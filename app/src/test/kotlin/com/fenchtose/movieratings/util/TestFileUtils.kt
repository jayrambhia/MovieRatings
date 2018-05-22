package com.fenchtose.movieratings.util

import android.content.Context
import android.net.Uri
import com.nhaarman.mockito_kotlin.mock

class TestFileUtils: FileUtils {
    override fun export(context: Context, uri: Uri, data: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readUri(context: Context, uri: Uri): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createCacheFile(context: Context, filename: String): Uri {
        return mock()
    }

}