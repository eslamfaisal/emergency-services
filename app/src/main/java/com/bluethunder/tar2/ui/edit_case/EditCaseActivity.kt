package com.bluethunder.tar2.ui.edit_case

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bluethunder.tar2.databinding.ActivityEditCaseBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory


class EditCaseActivity : AppCompatActivity() {

    val viewModel by viewModels<EditCaseViewModel> { getViewModelFactory() }

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
        viewModel.deviceLocationCheck.observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    requestLocationPermission()
                }
                Status.ERROR -> {
                    showRequestDeviceLocationErrorDialog()
                }
                Status.LOADING -> {
                    Log.e("EditCaseActivity", "Loading")
                }
                else -> {
                    Log.e("EditCaseActivity", "Unknown error")
                }
            }
        }
    }

    private fun showRequestDeviceLocationErrorDialog() {

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
            viewModel.requestLastLocation(this)
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
                    viewModel.requestLastLocation(this)
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
                if (resultCode == Activity.RESULT_OK) {
                    requestLocationPermission()
                } else {
                    Log.i(TAG, "User denied request")
                }
            }
        }
    }

    companion object {
        const val EXTRA_IS_NEW_CASE = "com.bluethunder.tar2.ui.edit_case.EXTRA_IS_NEW_CASE"
        private const val REQUEST_SHOW_SETTINGS = 123
        const val REQUEST_LOCATION_PERMISSION = 124
        private const val TAG = "EditCaseActivity"

        //https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/location-develop-steps-0000001050746143-V5#EN-US_TOPIC_0000001050746143__section090594025818
    }
}