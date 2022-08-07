package com.bluethunder.tar2.services

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage


class HmsPushService : HmsMessageService() {

    private val TAG = "HmsPushService"
    override fun onNewToken(token: String) {
        Log.i(TAG, "received refresh token:$token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: remoteMessage: $remoteMessage")

    }

}