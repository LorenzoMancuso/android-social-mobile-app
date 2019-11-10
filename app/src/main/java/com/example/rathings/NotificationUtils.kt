package com.example.rathings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.rathings.Card.DetailedCardActivity
import com.example.rathings.User.ProfileActivity
import com.example.rathings.utils.CustomObservable
import java.util.*
import kotlin.collections.ArrayList


object NotificationUtils {
    private var CHANNEL_ID = ""
    private var previousNotifications = ArrayList<Notification>()

    fun sendPushNotification(context: Context, notification: Notification){
        if (CHANNEL_ID == "")
            createNotificationChannel(context)

        val intent: Intent
        Log.d("[TARGET TYPE]", notification.targetType)
        if (notification.targetType == "card"){
            intent = Intent(context, DetailedCardActivity::class.java)
            intent.putExtra("idCard", notification.targetId)
        } else {
            intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("user", notification.idUser)
        }
        //intent.putExtra("mode", "options");

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star_border_red_24dp)
            .setContentTitle(context.getString(R.string.push_notification_title))
            .setContentText(context.getString(R.string.push_notification_message))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.push_notification_message)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

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
                Log.d("[PRE-NOTIFICATION]", previousNotifications.toString())
                Log.d("[NOTIFICATION]", notifications.toString())
                if (previousNotifications.size >0 && previousNotifications.size < notifications.size) {
                    val notification = notifications[0]
                    /**CONTROL FOR SEND PUSH NOTIFICATIONS*/
                    Log.d("[SEND NOTIFICATION]", notification.toString())
                    sendPushNotification(context, notification)
                }
                previousNotifications = notifications
                Log.d("[POST-NOTIFICATION]", previousNotifications.toString())
            }
        })
    }

}