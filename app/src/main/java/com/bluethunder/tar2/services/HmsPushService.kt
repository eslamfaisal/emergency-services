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
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import org.json.JSONException
import org.json.JSONObject


class HmsPushService : HmsMessageService() {

    private val TAG = "HmsPushService"
    override fun onNewToken(token: String) {
        Log.i(TAG, "received refresh token:$token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: remoteMessage: ${remoteMessage.data}")

        sendNotification(this, JSONObject(remoteMessage.data))
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param sendBird JSONObject payload from FCM
     */
    @Throws(JSONException::class)
    fun sendNotification(context: Context, sendBird: JSONObject) {
//
//        val message = sendBird.getString(StringSet.message)
//        val channel = sendBird.getJSONObject(StringSet.channel)
//        val channelUrl = channel.getString(StringSet.channel_url)
//        val channelCarName = channel.getString(StringSet.name)
//        var evalId = ""
//        evalId = if (channelUrl[0].toString().contains("i")) {
//            channelUrl.replace("i", "")
//        } else {
//            channelUrl.split("_").toTypedArray()[0]
//        }
//        Log.d(
//            com.algodriven.evalexpert.services.fcm.MyFirebaseMessagingService.TAG,
//            "sendNotification: eval id = $evalId"
//        )
//        val notificationManager =
//            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        val CHANNEL_ID: String = StringSet.CHANNEL_ID
//        if (Build.VERSION.SDK_INT >= 26) {  // Build.VERSION_CODES.O
//            val mChannel = NotificationChannel(
//                CHANNEL_ID,
//                StringSet.CHANNEL_NAME,
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationManager.createNotificationChannel(mChannel)
//        }
//        Log.d(
//            com.algodriven.evalexpert.services.fcm.MyFirebaseMessagingService.TAG,
//            "sendNotification: channelurl = $channelUrl"
//        )
//        val intent = Intent(context, HomeActivity::class.java)
//        intent.putExtra(Constants.CHANNEL_URL, channelUrl)
//        intent.putExtra(Constants.EVAL_ID, evalId)
//        intent.putExtra(Constants.CHANNEL_CAR_NAME, channelCarName)
//        intent.putExtra("came_from", "sendbird_notification")
//        intent.flags =
//            Intent.FLAG_ACTIVITY_NEW_TASK or if (HomeActivity.Companion.getInsideHomeActivity()) Intent.FLAG_ACTIVITY_CLEAR_TOP else Intent.FLAG_ACTIVITY_CLEAR_TASK
//        val pendingIntent =
//            PendingIntent.getActivity(context, channelUrl.hashCode() /* Request code */, intent, 0)
//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notificationBuilder: NotificationCompat.Builder =
//            NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
//                .setColor(ContextCompat.getColor(context, R.color.primary_300))
//                .setContentTitle(channelCarName)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setPriority(NotificationManager.IMPORTANCE_HIGH)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setContentIntent(pendingIntent)
//        notificationBuilder.setContentText(message)
//        notificationManager.notify(
//            System.currentTimeMillis().toString(),
//            0,
//            notificationBuilder.build()
//        )
    }

}