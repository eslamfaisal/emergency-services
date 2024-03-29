package com.bluethunder.tar2.ui.home.viewmodel

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.model.notifications.NotificationMessage
import com.bluethunder.tar2.model.notifications.NotificationRequestBody
import com.bluethunder.tar2.networking.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import com.huawei.agconnect.apms.APMS
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import com.huawei.hms.push.RemoteMessage
import kotlinx.coroutines.launch


class NotificationsViewModel(
    val context: Context
) : ViewModel() {

    companion object {
        private val TAG = NotificationsViewModel::class.java.simpleName
    }

    fun getToken() {
        // Create a thread.
        object : Thread() {
            override fun run() {
                try {
                    // Obtain the app ID from the agconnect-service.json file.
                    val appId = "106649263"

                    // Set tokenScope to HCM.
                    val tokenScope = "HCM"
                    val token = HmsInstanceId.getInstance(context).getToken(appId, tokenScope)
                    Log.i(TAG, "get token:$token")

                    // Check whether the token is empty.
                    if (!TextUtils.isEmpty(token)) {
                        sendRegTokenToServer(token)
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "get token failed, $e")
                }
            }
        }.start()
    }

    private fun sendRegTokenToServer(token: String?) {
        token?.let {
            Log.d(TAG, "sendRegTokenToServer: token = $token")
            updateUserToken(token)
            sbscribeTopic()
        }
    }

    private fun sbscribeTopic() {
        HmsMessaging.getInstance(context).subscribe("all").addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "subscribe topic success")
            } else {
                Log.e(TAG, "subscribe topic failed, ${it.exception}")
            }
        }
    }

    fun updateUserToken(token: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(SessionConstants.currentLoggedInUserModel!!.id!!)
            .update("pushToken", token)
            .addOnCompleteListener {
                Log.i(TAG, "update token success")
            }

//        sendMessage(token)
    }

    //https://forums.developer.huawei.com/forumPortal/en/topic/0201211024056890110
    fun sendMessage(message: String) {
        val builder = RemoteMessage.Builder("push.hcm.upstream")
            .setCollapseKey("-1")
            .setMessageId(System.currentTimeMillis().toString())
            .setMessageType("hms")
            .setTtl(120)
            .setData(mapOf("message" to message))
            .setSendMode(1)
            .setReceiptMode(1)

        val message: RemoteMessage = builder.build()
        HmsMessaging.getInstance(context).send(message)
    }

    fun getHMSAccessTokenAndSendNotification(isTopic: Boolean, sendTo: String, dataMap: String) {

        viewModelScope.launch {
            val customTrace = APMS.getInstance().createCustomTrace("create_new_case")
            try {

                customTrace.start()
                val tokenResponse = RetrofitClient.retrofitToken.gteHMSAccessToken()
                if (tokenResponse.isSuccessful) {
                    Log.d(TAG, "getHMSAccessToken: ${tokenResponse.body()!!.accessToken}")
                    val body =
                        NotificationRequestBody()
                    val messageBody =
                        NotificationMessage()

                    if (isTopic)
                        messageBody.topic = sendTo
                    else
                        messageBody.token = arrayOf(sendTo).toMutableList()

                    messageBody.data = dataMap
                    body.message = messageBody

                    val sendNotResponse = RetrofitClient.retrofitNotification.sendNotification(
                        tokenResponse.body()!!.accessToken,
                        body
                    )
                    if (sendNotResponse.isSuccessful) {
                        Log.d(TAG, "retrofitNotification: ${sendNotResponse.body()}")
                    } else {
                        Log.d(TAG, "retrofitNotification: ${sendNotResponse.errorBody()}")
                    }
                } else {
                    Log.d(TAG, "getHMSAccessToken: ${tokenResponse.errorBody()}")
                }
                customTrace.stop();
            } catch (e: Exception) {
                Log.d(TAG, "getHMSAccessToken error: ${e.message}")
                customTrace.stop();
            }
        }
    }
}
