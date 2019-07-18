package com.example.rathings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.rathings.utils.CustomObservable
import java.util.*
import kotlin.collections.ArrayList


object NotificationUtils {
    private var CHANNEL_ID = ""
    private var previousNotifications = ArrayList<Notification>()

    fun sendPushNotification(context: Context){
        if (CHANNEL_ID == "")
            createNotificationChannel(context)

        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star_border_red_24dp)
            .setContentTitle("You have a new notification!")
            .setContentText("Enter in Rathings for see what's up")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Enter in Rathings for see what's up"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify((System.currentTimeMillis() / 1000).toInt(), builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        CHANNEL_ID = "RATHINGS"
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rathings"
            val descriptionText = "Rathings notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificationListener(context: Context){
        FirebaseUtils.getNotifications().addObserver(object: Observer {
            override fun update(o: Observable?, arg: Any?) {
                val notifications = (o as CustomObservable).getValue() as ArrayList<Notification>
                if (previousNotifications.size >0 && previousNotifications.size < notifications.size) {
                    /**CONTROL FOR SEND PUSH NOTIFICATIONS*/
                    sendPushNotification(context)
                }
                previousNotifications = notifications
            }
        })
    }

}