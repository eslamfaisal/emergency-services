package com.bluethunder.tar2.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bluethunder.tar2.R
import com.bluethunder.tar2.model.notifications.NotificationDataModel
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.home.MainActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.google.gson.Gson
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import org.json.JSONException


class HmsPushService : HmsMessageService() {

    private val TAG = "HmsPushService"
    override fun onNewToken(token: String) {
        Log.i(TAG, "received refresh token:$token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: remoteMessage: ${remoteMessage.data}")

        sendNotification(this, (remoteMessage.data))
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param sendBird JSONObject payload from FCM
     */
    @Throws(JSONException::class)
    fun sendNotification(context: Context, data: String) {
        val currentLoggedInUserModel = Gson().fromJson(
            SharedHelper.getString(this, SharedHelperKeys.USER_DATA), UserModel::class.java
        )
        val dataModel = Gson().fromJson(data, NotificationDataModel::class.java)
        if (currentLoggedInUserModel.id == dataModel.userId) return


        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID: String = "StringSet.CHANNEL_ID"
        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                "StringSet.CHANNEL_NAME",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val intent = Intent(context, MainActivity::class.java)
//        intent.putExtra(Constants.CHANNEL_URL, channelUrl)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivity(context, 0 /* Request code */, intent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_huawie_logo)
                .setColor(ContextCompat.getColor(context, R.color.dark_red))
                .setContentTitle(dataModel.title)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
        notificationBuilder.setContentText(dataModel.description)
        notificationManager.notify(
            System.currentTimeMillis().toString(),
            0,
            notificationBuilder.build()
        )

    }

}