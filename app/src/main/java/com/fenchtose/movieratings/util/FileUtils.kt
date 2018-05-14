package com.fenchtose.movieratings.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v4.content.ContextCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class FileUtils {

    companion object {

        private fun hasPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        @SuppressLint("SimpleDateFormat")
        fun export(context: Context, data: String): String? {
            if (!hasPermission(context)) {
                return null
            }

            val filename = "flutter_${SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())}.txt"
            val base = File(Environment.getExternalStorageDirectory(), "Flutter")
            if (!base.exists() && !base.mkdirs()) {
                // could not create directory
                return null
            }

            val file = File(base, filename)
            if (!file.createNewFile()) {
                return null
            }

            file.writeText(data)
            /*context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(data.toByteArray())
            }*/

            return file.absolutePath
        }

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

    }
}