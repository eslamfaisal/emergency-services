package com.bluethunder.tar2.ui.home.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.databinding.FragmentHomeMapBinding
import com.bluethunder.tar2.model.Status.SUCCESS
import com.bluethunder.tar2.ui.MyLocationViewModel
import com.bluethunder.tar2.ui.chat.ChatHeadsActivity
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.home.adapter.CustomInfoWindowAdapter
import com.bluethunder.tar2.ui.home.viewmodel.MapScreenViewModel
import com.bluethunder.tar2.ui.scan.ScanCaseActivity
import com.bluethunder.tar2.ui.scan.ScanCaseActivity.Companion.SCAN_RESULT
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.*
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import com.huawei.hms.ml.scan.HmsScan


class HomeMapFragment : Fragment(), OnMapReadyCallback {

    private val myLocationViewModel by viewModels<MyLocationViewModel> { getViewModelFactory() }
    private val viewModel by viewModels<MapScreenViewModel> { getViewModelFactory() }
    private lateinit var binding: FragmentHomeMapBinding

    private lateinit var hmap: HuaweiMap
    private var mMarkers: MutableList<Marker> = ArrayList()

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

        if (!SharedHelper.getBoolean(requireActivity(), SharedHelperKeys.PERMISSIONS_REQUEST)) {
            SharedHelper.putBoolean(requireActivity(), SharedHelperKeys.PERMISSIONS_REQUEST, true)
            showRequestPermissionDialog()
        } else {
            requestLocationPermissions()
        }

        binding.qrScanner.setOnClickListener {
            requestCameraPermission()
        }
        initMapView()
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        repeat(permissions.entries.size) {
            startQrForResult()
        }
    }

    private val someActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val data: Intent? = result.data
                    val obj: HmsScan = data!!.getParcelableExtra(SCAN_RESULT)!!
                    Log.d(TAG, "scan result : ${obj.showResult}")
                    viewModel.getCaseDetailsAndOpenIt(requireActivity(), obj.showResult)
                } catch (e: Exception) {
                }
            }
        }


    private fun startQrForResult() {
        someActivityResultLauncher.launch(Intent(requireActivity(), ScanCaseActivity::class.java))
    }

    private fun requestCameraPermission() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.CAMERA,
            )
        )
    }

    private fun initViewModel() {
        myLocationViewModel.lastLocation.observe(viewLifecycleOwner) { resources ->
            when (resources.status) {
                SUCCESS -> {
                    resources.data?.let { location ->
                        SharedHelper.putString(
                            requireActivity(),
                            "last_lat",
                            location.latitude.toString()
                        )
                        SharedHelper.putString(
                            requireActivity(),
                            "last_lng",
                            location.longitude.toString()
                        )
                        SessionConstants.myCurrentLocation =
                            LatLng(location.latitude, location.longitude)
                        animateCameraToPosision(SessionConstants.myCurrentLocation!!, zoom = 10f)
                    }
                }
                else -> {

                }
            }
        }

        binding.messages.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    requireActivity(),
                    ChatHeadsActivity::class.java
                )
            )
        }
    }

    var isMapReady = false
    private fun initMapView() {
        if (isMapReady) return

        isMapReady = true
        MapsInitializer.setApiKey("DAEDADPF5OJAG21jxCnUWAX0InV9vW76SXWUaSMiIv81YAXW8bfCDMkAKKZ3lMU9mC2GCv78cYTZgeOIZ8OJkKXPg4ynC/CyrCUuvQ==")
        binding.mapView.onCreate(Bundle())

        Log.d(TAG, "onViewCreated: getMapAsync ")
        // get map by async method
        binding.mapView.getMapAsync(this)
        initViewModel()
    }


    val casesList = ArrayList<CaseModel>()
    private fun listenToCases() {
        viewModel.listenToCases()
        viewModel.casesList.observe(viewLifecycleOwner) { resources ->
            when (resources.status) {
                SUCCESS -> {
                    resources.data!!.forEach { document ->
                        try {
                            val case = document.document.toObject(CaseModel::class.java)
                            if (document.type == DocumentChange.Type.ADDED) {
                                casesList.remove(case)
                                if (case.caseDeleted) {
                                    removeMarkerFromMap(case)
                                } else {
                                    addMArkerToMAp(case)
                                    casesList.add(case)
                                }
                            } else if (document.type == DocumentChange.Type.REMOVED) {
                                removeMarkerFromMap(case)
                                casesList.remove(case)
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "listenToCases: ${e.message}")
                        }
                    }
                    binding.progressBar.visibility = View.GONE
                }
                else -> {

                }
            }
        }

    }

    fun getGeoCases() {

        val center = GeoLocation(
            SessionConstants.myCurrentLocation!!.latitude,
            SessionConstants.myCurrentLocation!!.longitude
        )
        val radiusInM = (50 * 1000).toDouble()


        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q: Query = FirebaseFirestore.getInstance()
                .collection(FirestoreReferences.CasesCollection.value())
                .orderBy("geohash")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
                for (task in tasks) {
                    val snap: QuerySnapshot = task.result
                    for (doc in snap.documents) {
                        try {
                            val lat = doc.getDouble("lat")!!
                            val lng = doc.getDouble("lng")!!

//                            addMArkerToMAp(lat, lng)

                            val docLocation = GeoLocation(lat, lng)
                            val distanceInM =
                                GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc)
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "getGeoCases: ${e.message}")
                        }
                    }
                }

            }
    }


    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, "onMapReady: map is ready")
        hmap = map

        try {
            val lastLat = SharedHelper.getString(requireActivity(), "last_lat")!!.toDouble()
            val lastLng = SharedHelper.getString(requireActivity(), "last_lng")!!.toDouble()
            SessionConstants.myCurrentLocation =
                LatLng(lastLat, lastLng)
            animateCameraToPosision(SessionConstants.myCurrentLocation!!, zoom = 10f)
        } catch (e: Exception) {
        }

        hmap.isMyLocationEnabled = true

        hmap.setOnMarkerClickListener { marker ->
            val isInfoWindowShown: Boolean = marker.isInfoWindowShown
            when {
                isInfoWindowShown -> {
                    marker.hideInfoWindow()
                }
                else -> {
                    marker.showInfoWindow()
                    try {
                        val case = (marker.tag as CaseModel)
                        animateCameraToPosision(
                            LatLng(case.lat, case.lng),
                            zoom = hmap.cameraPosition.zoom
                        )
                    } catch (e: Exception) {
                        Log.d(TAG, "onMapReady: ${e.message}")
                    }
                }
            }
            true
        }

        listenToCases()
//        getGeoCases()
    }

    private fun animateCameraToPosision(posision: LatLng, zoom: Float = 16f) {
        // move camera by CameraPosition param ,latlag and zoom params can set here
        val build = CameraPosition.Builder().target(posision).zoom(zoom).build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hmap.animateCamera(cameraUpdate)
    }

    @Throws(Exception::class)
    private fun addMArkerToMAp(case: CaseModel) {

        val options = MarkerOptions()
        options.position(LatLng(case.lat, case.lng))
        options.title(case.title)
        options.draggable(false)
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_emergency_case_red))
        options.clusterable(false)

        val customInfoWindow = CustomInfoWindowAdapter(requireActivity())
        hmap.setInfoWindowAdapter(customInfoWindow)
        val marker = hmap.addMarker(options)
        marker.tag = case
        marker.title = case.id
        mMarkers.add(marker)
    }

    private fun removeMarkerFromMap(case: CaseModel) {
        try {
            val marker = mMarkers.filter { it.title == case.id }.first()
            marker.remove()
        } catch (e: Exception) {
        }

    }

    fun showRequestPermissionDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(resources.getString(R.string.request_permission_message))
            .setCancelable(false)
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
        var allAccepted = true
        result.forEach {
            if (it.value) {
                Log.d(TAG, "onActivityResult: permission granted")
            } else {
                Log.d(TAG, "onActivityResult: permission denied")
                allAccepted = false
            }
        }
        if (allAccepted)
            requestLocationPermissions()
        else
            showRequestPermissionDialog()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            EditCaseActivity.REQUEST_DEVICE_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    requestLocationPermissions()
                } else {
                    Log.i(TAG, "User denied request")
                }
            }
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