package com.fenchtose.movieratings.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.fenchtose.movieratings.BuildConfig
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class AppFileUtils: FileUtils {
    override fun export(context: Context, uri: Uri, data: String): Boolean {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "w") ?: return false
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

    override fun readUri(context: Context, uri: Uri): String? {
        return context.contentResolver.openInputStream(uri)?.reader()?.readText()
    }

    override fun createCacheFile(context: Context, filename: String): Uri {
        val cacheFile = File(context.cacheDir, filename)
        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", cacheFile)
    }
}