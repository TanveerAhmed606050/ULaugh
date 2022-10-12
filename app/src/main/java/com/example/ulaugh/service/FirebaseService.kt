package com.example.ulaugh.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ulaugh.R
import com.example.ulaugh.controller.ProfileDetailActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {

    //    @Inject
//    private lateinit var sharePref: SharePref
    companion object {
//        var token = ""
//        var sharedPref: SharedPreferences? = null

//        var token: String?
//            get() {
//                return sharedPref?.getString("token", "")
//            }
//            set(value) {
//                sharedPref?.edit()?.putString("token", value)?.apply()
//            }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
//        sharedPref.writeString(Constants.MESSAGE_TOKEN, newToken)
//        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

//        val intent = Intent(this, ProfileDetailActivity::class.java)
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationID = Random.nextInt()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel(notificationManager)
//        }
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_MUTABLE)
//        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle(message.data["title"])
//            .setContentText(message.data["message"])
//            .setSmallIcon(com.example.ulaugh.R.drawable.ic_baseline_add_24)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        notificationManager.notify(notificationID, notification)
        startNotification(message)

    }

    private fun startNotification(message: RemoteMessage) {

        val notificationID = Random.nextInt()
        // adding action for activity
        val activityActionIntent = Intent(application, ProfileDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(application, 0, activityActionIntent, PendingIntent.FLAG_MUTABLE)
        val accept: PendingIntent =
            PendingIntent.getActivity(application, 1, activityActionIntent, PendingIntent.FLAG_MUTABLE)

        // adding action for broadcast
        val broadcastIntent = Intent(application, ProfileDetailActivity::class.java).apply {
            putExtra("action_msg", "some message for toast")
        }
        val reject: PendingIntent =
            PendingIntent.getBroadcast(application, 2, broadcastIntent, PendingIntent.FLAG_MUTABLE)
        val builder = NotificationCompat.Builder(application, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("${message.data}")
            .setContentText("${message.from}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("long notification content")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            //for adding action
            .addAction(R.drawable.user_logo, "Accept", accept)
            .addAction(R.drawable.bell, "Reject", reject)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(notificationID, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

}
