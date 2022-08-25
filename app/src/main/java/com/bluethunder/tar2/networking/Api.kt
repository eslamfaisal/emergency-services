package com.bluethunder.tar2.networking

import com.bluethunder.tar2.model.notifications.HMSAccessTokenResponse
import com.bluethunder.tar2.model.notifications.NotificationRequestBody
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceModel
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface Api {

    @Headers("Content-Type: application/json")
    @POST("mapApi/v1/routeService/driving?key=DAEDADPF5OJAG21jxCnUWAX0InV9vW76SXWUaSMiIv81YAXW8bfCDMkAKKZ3lMU9mC2GCv78cYTZgeOIZ8OJkKXPg4ynC/CyrCUuvQ==")
    fun getCaseDistance(
        @Body body: LocationDistanceRequestBody
    ): Call<LocationDistanceModel>

    @FormUrlEncoded
    @POST("oauth2/v3/token")
    suspend fun gteHMSAccessToken(
        @Field("grant_type") body: String = "client_credentials",
        @Field("client_id") clientID: String = "106649263",
        @Field("client_secret") clientSecret: String = "d32aff21440d2832fd15c1622ebfaf90fa3f4243b66f58d0836d85b50d1bfdfb",
    ): Response<HMSAccessTokenResponse>


    @POST("v1/106649263/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") authorization: String,
        @Body body: NotificationRequestBody
    ): Response<HMSAccessTokenResponse>


}