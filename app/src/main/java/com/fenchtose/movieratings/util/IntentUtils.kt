package com.fenchtose.movieratings.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Log
import com.fenchtose.movieratings.BuildConfig
import java.io.File

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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAYSTORE_URL))
            if (canStartIntent(context, intent)) {
                context.startActivity(intent)
            }
        }

        fun openShareIntent(context: Context, message: String) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "text/plain"
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }

        fun openShareFileIntent(context: Context, filename: String) {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(filename))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "text/plain"
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }

        fun getFileSelectionIntnet(): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            return intent
        }

        fun canStartIntent(context: Context, intent: Intent): Boolean {
            return intent.resolveActivity(context.packageManager) != null
        }

        fun openImdb(context: Context, imdb: String?) {
            imdb?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com/title/$imdb"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent.createChooser(intent, "Open IMDb page with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
}