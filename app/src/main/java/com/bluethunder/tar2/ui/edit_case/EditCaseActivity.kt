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
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.databinding.ActivityEditCaseBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore


class EditCaseActivity : AppCompatActivity() {

    val viewModel by viewModels<EditCaseViewModel> { getViewModelFactory() }

    private lateinit var binding: ActivityEditCaseBinding
    var isNewCase = false
    lateinit var mCurrentCase: CaseModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCaseBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        initIntentExtra()
        initViews()
        initViewModel()
    }

    private fun initIntentExtra() {
        isNewCase = intent.getBooleanExtra(EXTRA_IS_NEW_CASE, false)
        if (isNewCase) {
            mCurrentCase = CaseModel()
            mCurrentCase.id = FirebaseFirestore.getInstance().collection("cases").document().id
            mCurrentCase.userId = currentLoggedInUserModel!!.id
            mCurrentCase.userName = currentLoggedInUserModel!!.name
            mCurrentCase.userImage = currentLoggedInUserModel!!.imageUrl
        }
        viewModel.setCurrentCase(mCurrentCase)
    }

    private fun initViews() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initViewModel() {
        initObservers()
        viewModel.checkDeviceLocation(this)
        viewModel.selectedFragmentIndex.observe(this) {
            binding.caseDetailsTv.textSize = if (it == 0) 18f else 14f
            binding.personalDataTv.textSize = if (it == 1) 18f else 14f
            binding.caseLineView.setBackgroundColor(
                if (it == 0) resources.getColor(R.color.colorGreyLight) else resources.getColor(
                    R.color.colorBlack
                )
            )
            binding.pdCircleView.setCardBackgroundColor(
                if (it == 0) resources.getColor(R.color.colorGreyLight) else resources.getColor(
                    R.color.colorBlack
                )
            )
        }
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
            REQUEST_DEVICE_SETTINGS -> {
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
        const val REQUEST_DEVICE_SETTINGS = 123
        const val REQUEST_LOCATION_PERMISSION = 124
        private const val TAG = "EditCaseActivity"

        //https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/location-develop-steps-0000001050746143-V5#EN-US_TOPIC_0000001050746143__section090594025818
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
        super.onBackPressed()
    }
}