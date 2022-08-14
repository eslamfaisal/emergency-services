package com.bluethunder.tar2.ui.case_details.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
@Keep
data class LocationDistanceModel(
    @SerializedName("routes") var routes: ArrayList<Routes> = arrayListOf(),
    @SerializedName("returnCode") var returnCode: String? = null,
    @SerializedName("returnDesc") var returnDesc: String? = null
) : Parcelable

@Parcelize
@Keep
data class StartLocation(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable

@Parcelize
@Keep
data class Polyline(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable

@Parcelize
@Keep
data class Steps(

    @SerializedName("orientation") var orientation: Int? = null,
    @SerializedName("durationText") var durationText: String? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("roadName") var roadName: String? = null,
    @SerializedName("duration") var duration: Double? = null,
    @SerializedName("startLocation") var startLocation: StartLocation? = StartLocation(),
    @SerializedName("instruction") var instruction: String? = null,
    @SerializedName("action") var action: String? = null,
    @SerializedName("distanceText") var distanceText: String? = null,
    @SerializedName("endLocation") var endLocation: EndLocation? = EndLocation(),
    @SerializedName("polyline") var polyline: ArrayList<Polyline> = arrayListOf()
) : Parcelable

@Parcelize
@Keep
data class EndLocation(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable

@Parcelize
@Keep
data class Paths(
    @SerializedName("duration") var duration: Double? = null,
    @SerializedName("durationText") var durationText: String? = null,
    @SerializedName("durationInTrafficText") var durationInTrafficText: String? = null,
    @SerializedName("durationInTraffic") var durationInTraffic: Double? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("startLocation") var startLocation: StartLocation? = StartLocation(),
    @SerializedName("startAddress") var startAddress: String? = null,
    @SerializedName("distanceText") var distanceText: String? = null,
    @SerializedName("steps") var steps: ArrayList<Steps> = arrayListOf(),
    @SerializedName("endLocation") var endLocation: EndLocation? = EndLocation(),
    @SerializedName("endAddress") var endAddress: String? = null

) : Parcelable

@Parcelize
@Keep
data class Southwest(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable

@Parcelize
@Keep
data class Northeast(
    @SerializedName("lng") var lng: Double? = null,
    @SerializedName("lat") var lat: Double? = null
) : Parcelable

@Parcelize
@Keep
data class Bounds(
    @SerializedName("southwest") var southwest: Southwest? = Southwest(),
    @SerializedName("northeast") var northeast: Northeast? = Northeast()
) : Parcelable

@Parcelize
@Keep
data class Routes(
    @SerializedName("trafficLightNum") var trafficLightNum: Int? = null,
    @SerializedName("dstInDiffTimeZone") var dstInDiffTimeZone: Int? = null,
    @SerializedName("crossCountry") var crossCountry: Int? = null,
    @SerializedName("hasRestrictedRoad") var hasRestrictedRoad: Int? = null,
    @SerializedName("hasRoughRoad") var hasRoughRoad: Int? = null,
    @SerializedName("hasTrafficLight") var hasTrafficLight: Int? = null,
    @SerializedName("crossMultiCountries") var crossMultiCountries: Int? = null,
    @SerializedName("dstInRestrictedArea") var dstInRestrictedArea: Int? = null,
    @SerializedName("overviewPolyline") var overviewPolyline: String? = null,
    @SerializedName("paths") var paths: ArrayList<Paths> = arrayListOf(),
    @SerializedName("bounds") var bounds: Bounds? = Bounds(),
    @SerializedName("hasTolls") var hasTolls: Int? = null,
    @SerializedName("hasFerry") var hasFerry: Int? = null
) : Parcelable