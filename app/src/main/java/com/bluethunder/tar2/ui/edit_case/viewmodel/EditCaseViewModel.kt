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
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import java.io.File
import java.util.*


class EditCaseViewModel : ViewModel() {

    companion object {
        private val TAG = EditCaseViewModel::class.java.simpleName
    }

    var isNewCase: Boolean = true

    private val _selectedFragmentIndex = MutableLiveData(0)
    val selectedFragmentIndex: LiveData<Int> = _selectedFragmentIndex

    private val _currentCaseModel = MutableLiveData<CaseModel>()
    val currentCaseModel: LiveData<CaseModel> = _currentCaseModel

    private val _savingCaseModel = MutableLiveData<Resource<Boolean>>()
    val savingCaseModel: LiveData<Resource<Boolean>> = _savingCaseModel

    private val _deviceLocationCheck = MutableLiveData<Resource<String>>()
    val deviceLocationCheck: LiveData<Resource<String>> = _deviceLocationCheck

    private val _locationAddress = MutableLiveData<Resource<String>>()
    val locationAddress: LiveData<Resource<String>> = _locationAddress

    private val _lastLocation = MutableLiveData<Resource<Location>>()
    val lastLocation: LiveData<Resource<Location>> = _lastLocation

    private val _categories = MutableLiveData<Resource<MutableList<CaseCategoryModel>>>()
    val categories: LiveData<Resource<MutableList<CaseCategoryModel>>> = _categories

    private val _uploadingImage = MutableLiveData<Resource<Boolean>>()
    val uploadingImage: LiveData<Resource<Boolean>> = _uploadingImage

    fun checkDeviceLocation(activity: Activity) {
        val settingsClient = LocationServices.getSettingsClient(activity)
        val builder = LocationSettingsRequest.Builder()
        val mLocationRequest = getLocationRequest()

        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                setDeviceLocationCheckValue(Resource.success("Location settings are satisfied."))
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

    private val _caseCategory = MutableLiveData<CaseCategoryModel?>()
    val caseCategory: LiveData<CaseCategoryModel?> = _caseCategory
    fun getCaseCategory() {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CaseCategoriesCollection.value())
            .document(currentCaseModel.value?.categoryId!!)
            .get().addOnCompleteListener {
                try {
                    if (it.isSuccessful) {
                        val category = it.result.toObject(CaseCategoryModel::class.java)
                        _caseCategory.value = category
                    } else {
                    }
                } catch (e: Exception) {
                }

            }
    }

    fun setDeviceLocationCheckValue(resource: Resource<String>) {
        _deviceLocationCheck.value = resource
    }

    fun setSavingCaseModelValue(resource: Resource<Boolean>) {
        _savingCaseModel.value = resource
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

                            val case = currentCaseModel.value!!
                            case.latitude = it.latitude.toString()
                            case.longitude = it.longitude.toString()
                            setCurrentCase(case)

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
        try {
            val geocoder = Geocoder(activity, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val case = currentCaseModel.value!!
                case.locationName = address.getAddressLine(0)
                setCurrentCase(case)
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

    fun getCategories() {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CaseCategoriesCollection.value()).get()
            .addOnSuccessListener {
                val categories = mutableListOf<CaseCategoryModel>()
                it.forEach {
                    categories.add(it.toObject(CaseCategoryModel::class.java))
                }
                setCategoriesValue(Resource.success(categories))
            }.addOnFailureListener {
                setCategoriesValue(Resource.error(it.message))
            }
    }

    fun uploadMainCaseImage() {
        setMainImageLoading(Resource.loading())

        val reference =
            CloudStorageWrapper.storageManagement.getStorageReference("cases/${System.currentTimeMillis()}.jpg")
        val uploadTask = reference.putFile(File(profileImageLocalPath))
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.storage.downloadUrl.addOnSuccessListener {
                    profileImageUrl = it.toString()
                    val case = currentCaseModel.value!!
                    case.mainImage = profileImageUrl
                    setCurrentCase(case)
                    imageUploaded = true

                    Log.d(TAG, "uploadProfileImage: ${profileImageUrl}}")
                    setMainImageLoading(Resource.success(true))
                }.addOnFailureListener {
                    setMainImageLoading(Resource.error(it.message))
                }
            } else {
                setMainImageLoading(Resource.error(task.exception?.message))
            }
        }
    }

    private fun setMainImageLoading(loading: Resource<Boolean>) {
        _uploadingImage.value = loading
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

    fun getProfileImageLocalPath(): String {
        return profileImageLocalPath
    }

    fun setImageSelected(selected: Boolean) {
        imageSelected = selected
        if (!imageSelected) {
            profileImageUrl = ""
        }
        imageUploaded = false
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

    fun reverseShowPersonalData() {
        val case = currentCaseModel.value!!
        case.showUserData = !case.showUserData
        setCurrentCase(case)
    }

    fun handleCaseCallViewPhoneNumber() {
        val case = currentCaseModel.value!!
        case.hasPhoneCall = !case.hasPhoneCall
        setCurrentCase(case)
    }

    fun handleCallViaChatMessages() {
        val case = currentCaseModel.value!!
        case.hasChatMessages = !case.hasChatMessages
        setCurrentCase(case)
    }

    fun handleCallViaVideoCall() {
        val case = currentCaseModel.value!!
        case.hasVideoCall = !case.hasVideoCall
        setCurrentCase(case)
    }

    fun saveCase() {
        setSavingCaseModelValue(Resource.loading())
        FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
            .document(currentCaseModel.value!!.id!!).set(currentCaseModel.value!!)
            .addOnSuccessListener {
                saveCaseGeoLocation()
                Log.d(TAG, "saveCase: onSuccess")
                setSavingCaseModelValue(Resource.success(true))

            }.addOnFailureListener {
                Log.d(TAG, "saveCase: onFailure")
                setSavingCaseModelValue(Resource.error(it.message))
            }
    }

    fun saveCaseGeoLocation() {
        try {
            val lat = currentCaseModel.value!!.latitude!!.toDouble()
            val lng = currentCaseModel.value!!.longitude!!.toDouble()
            val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(lat, lng))

            val updates: MutableMap<String, Any> = HashMap()
            updates["geohash"] = hash
            updates["lat"] = lat
            updates["lng"] = lng

            FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
                .document(currentCaseModel.value!!.id!!).update(updates)
                .addOnCompleteListener {
                    Log.d(TAG, "saveCase: onSuccess")
                }
        }catch (e: Exception) {
            Log.d(TAG, "saveCase: onFailure")
        }

    }

    fun setCaseTitle(toString: String) {
        val case = currentCaseModel.value!!
        case.title = toString
        setCurrentCase(case)
    }

    fun setCaseCategory(id: String?) {
        val case = currentCaseModel.value!!
        case.categoryId = id
        setCurrentCase(case)
    }

    fun setCaseLocation(toString: String) {
        val case = currentCaseModel.value!!
        case.locationName = toString
        setCurrentCase(case)
    }

    fun setCaseDescription(toString: String) {
        val case = currentCaseModel.value!!
        case.description = toString
        setCurrentCase(case)
    }


}
