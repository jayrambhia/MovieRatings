package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fenchtose.movieratings.MainActivity
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.GaLabels
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.premium.DonatePageFragment


fun showSupportAppNotification(context: Context, category: String) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("fa_event", AppEvents.openNotification(GaLabels.NOTIFICATION_SUPPORT_APP).parcel())
    intent.putExtra(Router.HISTORY,
            Router.History()
                    .addPath(DonatePageFragment.DonatePath.KEY, DonatePageFragment.DonatePath.createExtras())
                    .toBundle())
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    // Use PendingIntent.FLAG_UPDATE_CURRENT to avoid the issue of android caching the pending intent
    val pendingIntent = PendingIntent.getActivity(context, Constants.SUPPORT_APP_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    showNotification(context, R.string.support_app_notification_title,
            R.string.support_app_notification_content,
            Constants.SUPPORT_APP_NOTIFICATION_ID,
            pendingIntent
    )

    AppEvents.sendNotification(category, GaLabels.NOTIFICATION_SUPPORT_APP).track()
}

fun showReviewAppNotification(context: Context, category: String) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("fa_event", AppEvents.openNotification(GaLabels.NOTIFICATION_RATE_APP).parcel())
    intent.putExtra(Router.HISTORY, Router.History().addPath("RateApp", Bundle()).toBundle())
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    // Use PendingIntent.FLAG_UPDATE_CURRENT to avoid the issue of android caching the pending intent
    val pendingIntent = PendingIntent.getActivity(context, Constants.REVIEW_APP_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    showNotification(context, R.string.review_app_notification_title,
            R.string.review_app_notification_content,
            Constants.REVIEW_APP_NOTIFICATION_ID,
            pendingIntent
    )

    AppEvents.sendNotification(category, GaLabels.NOTIFICATION_RATE_APP).track()
}

private fun showNotification(context: Context, @StringRes title: Int, @StringRes content: Int,
                             notificationId: Int, intent: PendingIntent? = null) {
    val builder = NotificationCompat.Builder(context, Constants.SUPPORT_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_logo)
            .setContentTitle(context.getString(title))
            .setContentText(context.getString(content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(content)))
            .setAutoCancel(true)

    intent?.let {
        builder.setContentIntent(intent)
    }

    val manager = NotificationManagerCompat.from(context)
    manager.notify(notificationId, builder.build())
}

@SuppressLint("NewApi")
fun registerNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(Constants.SUPPORT_CHANNEL_ID,
                context.getString(R.string.support_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW)
        channel.description = context.getString(R.string.support_notification_channel_description)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

@SuppressLint("NewApi")
fun isNotificationChannelBlocked(context: Context, channelId: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel(channelId)
        return channel == null || channel.importance == NotificationManager.IMPORTANCE_NONE
    }

    return false
}