package com.fenchtose.movieratings.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.fenchtose.movieratings.BuildConfig
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

interface FileUtils {
    fun export(context: Context, uri: Uri, data: String): Boolean
    fun readUri(context: Context, uri: Uri): String?
    fun createCacheFile(context: Context, filename: String): Uri
}

