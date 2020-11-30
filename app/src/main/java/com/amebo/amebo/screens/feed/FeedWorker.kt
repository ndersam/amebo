package com.amebo.amebo.screens.feed

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat
import androidx.work.*
import com.amebo.amebo.R
import com.amebo.amebo.application.App
import com.amebo.amebo.application.MainActivity
import com.amebo.core.CoreUtils
import com.amebo.core.Nairaland
import com.amebo.core.domain.ResultWrapper
import com.amebo.core.domain.TopicFeed
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class FeedWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val nairaland: Nairaland
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var isSuccess = false
        runBlocking {
            val result = nairaland.sources.misc.feed()

            if (result is ResultWrapper.Success) {
                isSuccess = true
                notifyUser(result.data)
            }
        }
        return if (isSuccess) Result.success() else Result.failure()
    }

    private fun notifyUser(data: List<TopicFeed>) {

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = MainActivity.INTENT_ACTION_TOPIC
            putExtra(MainActivity.INTENT_EXTRA_TOPIC, data.first().topic)
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        createNotificationChannel()
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.launcher_icon)
            .setContentTitle(applicationContext.getString(R.string.featured_topic))
            .setContentText(notificationTitle(data))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(notificationBody(data))
            ).setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.channel_name)
            val descriptionText =
                applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notificationTitle(topics: List<TopicFeed>): CharSequence {
        return HtmlCompat.fromHtml(
            "<b>${topics.first().topic.title}</b>",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    private fun notificationBody(topics: List<TopicFeed>): CharSequence {
        val topic = topics.first()
        return HtmlCompat.fromHtml(
            "<b>${topic.topic.title}</b><br>${CoreUtils.cleanHTML(
                topic.summary
            )}", HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "com.amebo.amebo.features.feed.FeedWorker"
        private const val UNIQUE_WORK_NAME = "com.amebo.amebo.features.feed.FeedWorker"

        fun schedule(context: App) {
            WorkManager.initialize(context, context.workerConfiguration)
            val request = PeriodicWorkRequestBuilder<FeedWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
//            val request = OneTimeWorkRequestBuilder<FeedWorker>().build()
//            WorkManager.getInstance(context)
//                .enqueue(request)
        }
    }
}