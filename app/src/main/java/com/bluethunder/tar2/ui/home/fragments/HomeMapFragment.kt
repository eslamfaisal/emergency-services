package com.bluethunder.tar2.ui.home.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.databinding.FragmentHomeMapBinding
import com.bluethunder.tar2.model.Status.SUCCESS
import com.bluethunder.tar2.ui.MyLocationViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.home.viewmodel.MapScreenViewModel
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*


class HomeMapFragment : Fragment(), OnMapReadyCallback {

    private val myLocationViewModel by viewModels<MyLocationViewModel> { getViewModelFactory() }
    private val viewModel by viewModels<MapScreenViewModel> { getViewModelFactory() }
    private lateinit var binding: FragmentHomeMapBinding

    private lateinit var hmap: HuaweiMap
    private var mMarker: Marker? = null
    private var mCircle: Circle? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home_map, container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // endregion
        if (!SharedHelper.getBoolean(requireActivity(), SharedHelperKeys.PERMISSIONS_REQUEST)) {
            SharedHelper.putBoolean(requireActivity(), SharedHelperKeys.PERMISSIONS_REQUEST, true)
            showRequestPermissionDialog()
        } else {
            requestLocationPermissions()
        }

        initViewModel()
    }

    private fun initViewModel() {
        myLocationViewModel.lastLocation.observe(viewLifecycleOwner) { resources ->
            when (resources.status) {
                SUCCESS -> {
                    resources.data?.let { location ->
                        SessionConstants.myCurrentLocation =
                            LatLng(location.latitude, location.longitude)
                        initMapView()
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun initMapView() {
        MapsInitializer.setApiKey("DAEDADPF5OJAG21jxCnUWAX0InV9vW76SXWUaSMiIv81YAXW8bfCDMkAKKZ3lMU9mC2GCv78cYTZgeOIZ8OJkKXPg4ynC/CyrCUuvQ==")
        binding.mapView.onCreate(Bundle())

        Log.d(TAG, "onViewCreated: getMapAsync ")
        // get map by async method
        binding.mapView.getMapAsync(this)
    }

    fun showRequestPermissionDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(resources.getString(R.string.request_permission_message))
            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                // Respond to negative button press
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                // Respond to positive button press
                requestLocationPermissions()
                dialog.dismiss()
            }
            .show()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        requestLocationPermissions()
    }


    private fun requestLocationPermissions() {
        if (!hasPermissions(requireActivity(), *RUNTIME_PERMISSIONS)) {
            requestPermissionLauncher.launch(
                RUNTIME_PERMISSIONS
            )
        } else {
            myLocationViewModel.checkDeviceLocation(requireActivity(), true)
        }

    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onMapReady(map: HuaweiMap) {

        Log.d(TAG, "onMapReady: map is ready")
        // after call getMapAsync method ,we can get HuaweiMap instance in this call back method
        hmap = map
        hmap.isMyLocationEnabled = true

        // move camera by CameraPosition param ,latlag and zoom params can set here
        val build = CameraPosition.Builder().target(LatLng(60.0, 60.0)).zoom(5f).build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hmap.animateCamera(cameraUpdate)

        // mark can be add by HuaweiMap
        mMarker = hmap.addMarker(
            MarkerOptions().position(LAT_LNG)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_emergency_case_red))
                .anchorMarker(0.5f, 0.5f)
                .clusterable(true)
        )
        mMarker?.showInfoWindow()

        // circle can be add by HuaweiMap
        mCircle = hmap.addCircle(
            CircleOptions().center(LatLng(60.0, 60.0)).radius(5000.0).fillColor(
                Color.GREEN
            )
        )
        mCircle?.fillColor = Color.TRANSPARENT

        binding.progressBar.visibility = View.GONE
    }


    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }


    companion object {
        private const val TAG = "MapViewDemoActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val REQUEST_CODE = 100
        private val LAT_LNG = LatLng(30.2567239, 31.1561441)
        private val RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )
    }

}