package com.bluethunder.tar2.ui.edit_case.viewmodel

import android.app.Activity
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.cloud_db.CloudDBWrapper
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import java.util.*


class EditCaseViewModel : ViewModel() {

    companion object {
        private val TAG = EditCaseViewModel::class.java.simpleName
    }

    private val _selectedFragmentIndex = MutableLiveData(0)
    val selectedFragmentIndex: LiveData<Int> = _selectedFragmentIndex

    private val _currentCaseModel = MutableLiveData<CaseModel>()
    val currentCaseModel: LiveData<CaseModel> = _currentCaseModel

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _deviceLocationCheck = MutableLiveData<Resource<String>>()
    val deviceLocationCheck: LiveData<Resource<String>> = _deviceLocationCheck

    private val _locationAddress = MutableLiveData<Resource<String>>()
    val locationAddress: LiveData<Resource<String>> = _locationAddress

    private val _lastLocation = MutableLiveData<Resource<Location>>()
    val lastLocation: LiveData<Resource<Location>> = _lastLocation

    private val _categories = MutableLiveData<Resource<MutableList<CaseCategoryModel>>>()
    val categories: LiveData<Resource<MutableList<CaseCategoryModel>>> = _categories


    fun checkDeviceLocation(activity: Activity) {
        val settingsClient = LocationServices.getSettingsClient(activity)
        val builder = LocationSettingsRequest.Builder()
        val mLocationRequest = getLocationRequest()

        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        settingsClient.checkLocationSettings(locationSettingsRequest) // Define callback for success in checking the device location settings.
            .addOnSuccessListener {
                setDeviceLocationCheckValue(Resource.success("Location settings are satisfied."))
            } // Define callback for failure in checking the device location settings.
            .addOnFailureListener { e ->
                // Device location settings do not meet the requirements.
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae: ResolvableApiException = e as ResolvableApiException
                        // Call startResolutionForResult to display a pop-up asking the user to enable related permission.
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

    fun requestLastLocation(activity: Activity) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                setLastLocationValue(Resource.success(location))
//                getLocationName(location, activity)
//            }
//        }

        fusedLocationProviderClient
            .requestLocationUpdates(
                getLocationRequest(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            Log.d("EditCaseActivity", "onLocationResult: $it")
                            Log.d("EditCaseActivity", "onLocationResult: ${it.latitude}")
                            Log.d("EditCaseActivity", "onLocationResult: ${it.longitude}")

                            setLastLocationValue(Resource.success(it))
                            getLocationName(it, activity)
                        }
                    }
                },
                Looper.getMainLooper()
            ).addOnSuccessListener {
                Log.d(TAG, "requestLastLocation: onSuccess")
            }

    }

    fun getLocationName(location: Location, activity: Activity) {
        val geocoder = Geocoder(activity, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses.isNotEmpty()) {
            val address = addresses[0]
            setAddressValue(Resource.success(address.getAddressLine(0)))
        }

    }

    private fun getLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 2000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return mLocationRequest
    }

    fun getCategories() {
        val query = CloudDBZoneQuery.where(CaseCategoryModel::class.java)
            .orderByDesc("priority")
        CloudDBWrapper.mTar2APPCloudDBZone!!.executeQuery(
            query,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_DEFAULT
        ).addOnCompleteListener {
            if (it.isSuccessful) {

                val bookInfoCursor = it.result.snapshotObjects
                val categoriesList: MutableList<CaseCategoryModel> = ArrayList()
                try {
                    while (bookInfoCursor.hasNext()) {
                        val bookInfo = bookInfoCursor.next()
                        categoriesList.add(bookInfo)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Log.w(TAG, "processQueryResult: " + e.message)
                } finally {
                    it.result.release()
                }

                setCategoriesValue(Resource.success(categoriesList))
            } else {
                setCategoriesValue(Resource.error(it.exception?.message))
            }
        }
    }

    private fun setCategoriesValue(success: Resource<MutableList<CaseCategoryModel>>) {
        _categories.value = success
    }

    private fun setLastLocationValue(success: Resource<Location>) {
        _lastLocation.value = success
    }

    private fun setAddressValue(toString: Resource<String>) {
        _locationAddress.value = toString
    }

    fun setCurrentCase(mCurrentCase: CaseModel) {
        _currentCaseModel.value = mCurrentCase
    }

    fun setSelectedFragmentIndex(index: Int) {
        _selectedFragmentIndex.value = index
    }

    private var profileImageLocalPath: String = ""
    var profileImageUrl: String = ""
    private var imageSelected = false
    var imageUploaded = false

    fun setProfileImageLocalPath(path: String) {
        profileImageLocalPath = path
        setImageSelected(true)
    }

    fun setImageSelected(selected: Boolean) {
        imageSelected = selected
        if (!imageSelected) {
            profileImageUrl = ""
            imageUploaded = false
        }
    }

    fun removeImage() {
        setImageSelected(false)
    }

    fun isImageSelected(): Boolean {
        return imageSelected
    }

    fun onBackPressed() {
        if (selectedFragmentIndex.value!! > 0) {
            setSelectedFragmentIndex(0)
        }
    }

}
