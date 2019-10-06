package com.example.rathings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
            .setContentTitle("You have a new notification!")
            .setContentText("Enter in Rathings for see what's up")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Enter in Rathings for see what's up"))
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
                if (previousNotifications.size >0 && previousNotifications.size < notifications.size) {
                    val notification = notifications[notifications.size-1]
                    /**CONTROL FOR SEND PUSH NOTIFICATIONS*/
                    sendPushNotification(context, notification)
                }
                previousNotifications = notifications
            }
        })
    }

}