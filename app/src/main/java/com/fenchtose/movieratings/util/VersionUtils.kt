package com.fenchtose.movieratings.util

import android.os.Build

class VersionUtils {
    companion object {
        fun isMOrAbove(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        }
    }
}
