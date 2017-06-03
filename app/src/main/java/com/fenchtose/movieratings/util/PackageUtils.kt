package com.fenchtose.movieratings.util

import android.content.Context

class PackageUtils {

    companion object {
        val NETFLIX = "com.netflix.mediaservice"

        fun hasInstalled(context: Context, packageName: String): Boolean {
            val packages = context.packageManager.getInstalledApplications(0)
            packages.forEach {
                if (it.packageName == packageName) {
                    return true
                }
            }
            return false
        }
    }

}