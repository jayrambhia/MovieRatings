package com.fenchtose.movieratings.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v4.content.ContextCompat
import java.io.File
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

        fun readUri(context: Context, uri: Uri): String {
            return context.contentResolver.openInputStream(uri).reader().readText()
        }

    }
}