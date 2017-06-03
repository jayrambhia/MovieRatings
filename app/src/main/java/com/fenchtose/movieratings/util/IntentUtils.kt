package com.fenchtose.movieratings.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fenchtose.movieratings.BuildConfig

class IntentUtils {

    companion object {

        val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID

        fun launch3rdParty(context: Context, packageName: String) {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent)
            }
        }

        fun openPlaystore(context: Context) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAYSTORE_URL)))
        }

        fun openShareIntent(context: Context, message: String) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "text/plain"
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }
    }
}