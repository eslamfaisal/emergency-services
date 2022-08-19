package com.bluethunder.tar2.ui

import android.app.Activity
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import java.util.*

class MyLocationViewModel : ViewModel() {

    companion object {
        private val TAG = MyLocationViewModel::class.java.simpleName
    }

    private val _deviceLocationCheck = MutableLiveData<Resource<String>>()
    val deviceLocationCheck: LiveData<Resource<String>> = _deviceLocationCheck

    private val _locationAddress = MutableLiveData<Resource<String>>()
    val locationAddress: LiveData<Resource<String>> = _locationAddress

    private val _lastLocation = MutableLiveData<Resource<Location?>>(Resource.empty())
    val lastLocation: LiveData<Resource<Location?>> = _lastLocation


    fun checkDeviceLocation(activity: Activity, oneTimeRequest: Boolean = false) {
        val settingsClient = LocationServices.getSettingsClient(activity)
        val builder = LocationSettingsRequest.Builder()
        val mLocationRequest = getLocationRequest()

        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                requestLastLocation(activity, oneTimeRequest)
            }.addOnFailureListener { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae: ResolvableApiException = e as ResolvableApiException
                        rae.startResolutionForResult(
                            activity,
                            EditCaseActivity.REQUEST_DEVICE_SETTINGS
                        )
                    } catch (sie: IntentSender.SendIntentException) {
                        setDeviceLocationCheckValue(Resource.error("Unable to resolve location settings: ${sie.message}"))
                    }
                }
            }
    }

    fun setDeviceLocationCheckValue(resource: Resource<String>) {
        _deviceLocationCheck.value = resource
    }


    fun requestLastLocation(activity: Activity, oneTime: Boolean = false) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                setLastLocationValue(Resource.success(location))
//                getLocationName(location, activity)
//            }
//        }

        val callBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (oneTime)
                    fusedLocationProviderClient.removeLocationUpdates(this)
                locationResult.lastLocation?.let {
                    setLastLocationValue(Resource.success(it))
                    getLocationName(it, activity)

                }
            }
        }
        fusedLocationProviderClient
            .requestLocationUpdates(
                getLocationRequest(),
                callBack,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                Log.d(TAG, "requestLastLocation: onSuccess")
            }
    }

    fun getLocationName(location: Location, activity: Activity) {
        try {
            val geocoder = Geocoder(activity, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                setAddressValue(Resource.success(address.getAddressLine(0)))
            }
        } catch (e: Exception) {
        }

    }

    private fun getLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 2000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return mLocationRequest
    }

    private fun setLastLocationValue(success: Resource<Location?>) {
        _lastLocation.value = success
    }

    private fun setAddressValue(toString: Resource<String>) {
        _locationAddress.value = toString
    }

}
