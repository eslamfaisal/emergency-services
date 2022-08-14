package com.bluethunder.tar2.ui.case_details.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class LocationDistanceRequestBody(
    @SerializedName("origin") var origin: Origin? = Origin(),
    @SerializedName("destination") var destination: Destination? = Destination()
) : Parcelable

@Parcelize
@Keep
data class Origin(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable

@Parcelize
@Keep
data class Destination(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable