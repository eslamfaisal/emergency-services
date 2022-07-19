package com.bluethunder.tar2.ui.edit_case

import android.Manifest
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bluethunder.tar2.databinding.ActivityEditCaseBinding
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*


class EditCaseActivity : AppCompatActivity() {

    private val viewModel by viewModels<EditCaseViewModel> { getViewModelFactory() }

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
        checkDeviceLocation()
    }

    fun requestLocationPermission() {
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
            ActivityCompat.requestPermissions(this, strings, REQUEST_LOCATION_PERMISSION)
        } else {
            requestLastLocation()
        }
    }


    fun requestLastLocation() {

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient
            .requestLocationUpdates(
                getLocationRequest(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            Log.d("EditCaseActivity", "onLocationResult: $it")
                            Log.d("EditCaseActivity", "onLocationResult: ${it.latitude}")
                            Log.d("EditCaseActivity", "onLocationResult: ${it.longitude}")
                        }
                    }

                },
                Looper.getMainLooper()
            ).addOnSuccessListener {
                Log.d(TAG, "requestLastLocation: onSuccess")
            }
    }

    fun checkDeviceLocation() {

        val settingsClient = LocationServices.getSettingsClient(this)
        val builder = LocationSettingsRequest.Builder()
        val mLocationRequest = getLocationRequest()

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

    private fun getLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 2000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return mLocationRequest
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
                   requestLastLocation()
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