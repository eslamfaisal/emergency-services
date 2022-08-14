package com.bluethunder.tar2.networking

import com.bluethunder.tar2.ui.case_details.model.LocationDistanceModel
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {

    @Headers("Content-Type: application/json")
    @POST("mapApi/v1/routeService/driving?key=DAEDADPF5OJAG21jxCnUWAX0InV9vW76SXWUaSMiIv81YAXW8bfCDMkAKKZ3lMU9mC2GCv78cYTZgeOIZ8OJkKXPg4ynC/CyrCUuvQ==")
    fun getCaseDistance(
        @Body body: LocationDistanceRequestBody
    ): Call<LocationDistanceModel>

}