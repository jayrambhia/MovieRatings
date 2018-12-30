package com.fenchtose.movieratings.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MainActivity
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.moviepage.MoviePath
import com.fenchtose.movieratings.model.entity.MovieRating

class IntentUtils {

    companion object {

        val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID

        fun launch3rdParty(activity: Activity, packageName: String): Boolean {
            val intent = activity.packageManager.getLaunchIntentForPackage(packageName)
            return if (intent != null && PackageUtils.isIntentCallabale(activity, intent)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                true
            } else {
                false
            }
        }

        fun openSettings(context: Context): Boolean {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            return if (PackageUtils.isIntentCallabale(context, intent)) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun openBatteryOptimizationWhitelist(context: Context): Boolean {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            return if (PackageUtils.isIntentCallabale(context, intent)) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun openDrawSettings(context: Context): Boolean {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName))
            return if (PackageUtils.isIntentCallabale(context, intent)) {
                context.startActivity(intent)
                true
            } else {
                false
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

        fun openReportBugIntent(context: Context, body: String) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                type = "text/plain"
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("flutter.rating@gmail.com"))
                putExtra(Intent.EXTRA_TEXT, body)
            }

            context.startActivity(Intent.createChooser(intent, "Report bug via"))
        }

        fun openPrivacyPolicy(context: Context) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://raw.githubusercontent.com/jayrambhia/MovieRatings/master/privacy_policy.txt"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(Intent.createChooser(intent, "Open privacy policy with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        fun openShareFileIntent(context: Context, uri: Uri) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_TITLE, "flutter_data.txt")
            intent.type = "text/plain"
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }

        fun getFileSelectionIntent(): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            return intent
        }

        fun getFileCreationIntent(filename: String): Intent {
            return Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TITLE, filename)
        }

        fun canStartIntent(context: Context, intent: Intent): Boolean {
            return intent.resolveActivity(context.packageManager) != null
        }

        fun openImdb(context: Context, imdb: String?, newTask: Boolean = true): Boolean {
            imdb?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com/title/$imdb"))
                if (newTask) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Open IMDb page with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            }

            return false
        }

        fun openMAL(context: Context, id: String?, newTask: Boolean = true): Boolean {
            id?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/anime/$id"))
                if (newTask) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Open My Anime List page with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            }

            return false
        }

        fun openMovie(context: Context, movie: MovieRating?, inApp: Boolean = false, newTask: Boolean = true): Boolean {
            movie?.let {
                return when(it.source) {
                    "IMDB" -> {
                        if (!inApp) {
                            return openImdb(context, it.imdbId, newTask)
                        }

                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra(Router.HISTORY,
                                Router.History()
                                        .addPath(MoviePath.KEY, MoviePath.createExtras(it.imdbId))
                                        .toBundle())
                        if (newTask) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        context.startActivity(intent)
                        true
                    }
                    "MAL" -> openMAL(context, it.imdbId, newTask)
                    else -> false
                }

            }

            return false
        }
    }
}