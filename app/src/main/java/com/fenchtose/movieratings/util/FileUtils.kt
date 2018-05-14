package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import com.fenchtose.movieratings.BuildConfig
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class FileUtils {

    companion object {

        private val TAG = "FileUtils"

        fun export(context: Context, uri: Uri, data: String): Boolean {
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "w")
                val fos = FileOutputStream(pfd.fileDescriptor)
                fos.write(data.toByteArray())
                fos.close()
                pfd.close()
                return true
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return false
        }

        fun readUri(context: Context, uri: Uri): String {
            return context.contentResolver.openInputStream(uri).reader().readText()
        }

        fun createCacheFile(context: Context, filename: String): Uri {
            val cacheFile = File.createTempFile(filename, ".txt", context.cacheDir)
            return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", cacheFile)
        }

        @SuppressLint("SimpleDateFormat")
        fun createCacheFilename(): String {
            return "flutter_${SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())}"
        }

    }
}