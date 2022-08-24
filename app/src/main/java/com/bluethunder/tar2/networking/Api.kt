package com.bluethunder.tar2.networking

import com.bluethunder.tar2.model.HMSAccessTokenResponse
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
    @POST("v1/106842921/messages:send")
    suspend fun gteHMSAccessToken(
        @Field("grant_type") body: String = "client_credentials",
        @Field("client_id") clientID: String = "106842921",
        @Field("client_secret") clientSecret: String = "5645b7ce975ca21ecac374256356b8a330bce83048fce100c99cc23dd77f551a",
    ): Response<HMSAccessTokenResponse>


}