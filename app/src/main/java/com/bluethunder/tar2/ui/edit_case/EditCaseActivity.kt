package com.bluethunder.tar2.ui.edit_case

import android.Manifest
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bluethunder.tar2.databinding.ActivityEditCaseBinding
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.edit_case.viewmodel.LocationViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException

import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationServices
import com.huawei.hms.location.LocationSettingsRequest
import com.huawei.hms.location.LocationSettingsStatusCodes
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation


class EditCaseActivity : AppCompatActivity() {

    private val viewModel by viewModels<EditCaseViewModel> { getViewModelFactory() }
    private val locationViewModel: LocationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LocationViewModel(
                    CoLocation.from(this@EditCaseActivity),
                    CoGeocoder.from(this@EditCaseActivity)
                ) as T
        }
    }

    private lateinit var binding: ActivityEditCaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCaseBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        initViews()
        initViewModel()
    }

    private fun initViews() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initViewModel() {
        initObservers()
        viewModel.setOnMapSelected(2)

    }

    private fun initObservers() {
        viewModel.onSelectedTabIndex.observe(this) {

        }

        lifecycle.addObserver(locationViewModel)
        locationViewModel.resolveSettingsEvent.observe(this) {
            it.resolve(
                this, REQUEST_SHOW_SETTINGS
            )
        }
        observeLocation()
    }

    private fun observeLocation() {
        locationViewModel.locationUpdates.observe(this, this::onLocationUpdate)
    }

    private fun onLocationUpdate(location: Location?) {
        if (location == null) return

        Log.d("EditCaseActivity", "onLocationUpdate: $location")
        Log.d("EditCaseActivity", "onLocationUpdate: ${location.latitude}")
        Log.d("EditCaseActivity", "onLocationUpdate: ${location.longitude}")

    }

    fun requestLocationPermission() {

        // Dynamically apply for required permissions if the API level is 28 or smaller.
        // Dynamically apply for required permissions if the API level is 28 or smaller.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "android sdk <= 28 Q")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(this, strings, 1)
            } else {
                observeLocation()
            }
        } else {
            // Dynamically apply for required permissions if the API level is greater than 28. The android.permission.ACCESS_BACKGROUND_LOCATION permission is required.
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                )
                ActivityCompat.requestPermissions(this, strings, 2)
            } else {
                observeLocation()
            }
        }
    }


    fun checkDeviceLocation() {
        val settingsClient = LocationServices.getSettingsClient(this)
        val builder = LocationSettingsRequest.Builder()
        val mLocationRequest = LocationRequest()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        settingsClient.checkLocationSettings(locationSettingsRequest) // Define callback for success in checking the device location settings.
            .addOnSuccessListener {
                requestLocationPermission()
            } // Define callback for failure in checking the device location settings.
            .addOnFailureListener { e ->
                // Device location settings do not meet the requirements.
                val statusCode: Int = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae: ResolvableApiException = e as ResolvableApiException
                        // Call startResolutionForResult to display a pop-up asking the user to enable related permission.
                        rae.startResolutionForResult(
                            this@EditCaseActivity,
                            REQUEST_LOCATION_PERMISSION
                        )
                    } catch (sie: SendIntentException) {
                        // ...
                    }
                }
            }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    observeLocation()
                } else {
                    Log.i(TAG, "Permission denied")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_SHOW_SETTINGS -> {
                requestLocationPermission()
            }
        }
    }

    companion object {
        const val EXTRA_IS_NEW_CASE = "com.bluethunder.tar2.ui.edit_case.EXTRA_IS_NEW_CASE"
        private const val REQUEST_SHOW_SETTINGS = 123
        private const val REQUEST_LOCATION_PERMISSION = 124
        private const val TAG = "EditCaseActivity"

        //https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/location-develop-steps-0000001050746143-V5#EN-US_TOPIC_0000001050746143__section090594025818
    }
}