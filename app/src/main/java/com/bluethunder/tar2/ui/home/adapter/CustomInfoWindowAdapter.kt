package com.bluethunder.tar2.ui.home.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.networking.RetrofitClient
import com.bluethunder.tar2.ui.case_details.CaseDetailsActivity
import com.bluethunder.tar2.ui.case_details.model.Destination
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceModel
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceRequestBody
import com.bluethunder.tar2.ui.case_details.model.Origin
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.bluethunder.tar2.utils.TimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomInfoWindowAdapter(val context: Activity) : HuaweiMap.InfoWindowAdapter {

    companion object {
        private const val TAG = "CustomInfoWindowAdapter"
    }

    override fun getInfoWindow(marker: Marker?): View {

        val mInfoView = context.layoutInflater.inflate(R.layout.custom_info_window, null)
        try {
            Log.d(TAG, "getInfoWindow: ${marker?.title}")
            val caseModel: CaseModel? = marker?.tag as CaseModel?
            val timeAgo = TimeAgo()
            timeAgo.locale(context)
            mInfoView.findViewById<TextView>(R.id.date_tv).text =
                timeAgo.getTimeAgo(caseModel?.createdAt!!)
            mInfoView.findViewById<TextView>(R.id.username_tv).text = caseModel.userName
            mInfoView.findViewById<TextView>(R.id.infoTile).text = caseModel.title
            mInfoView.findViewById<TextView>(R.id.infoAddress).text = caseModel.description

            caseModel.userImage?.let {
                setUserImage(
                    mInfoView.findViewById(R.id.profile_image),
                    it
                )
            }

            getCaseLocationDistance(
                mInfoView.findViewById(R.id.distance_progress_bar),
                mInfoView.findViewById(R.id.distance_tv),
                caseModel.lat,
                caseModel.lng,
            )

            mInfoView.findViewById<View>(R.id.location_direction_view).setOnClickListener {
                Log.d(TAG, "getInfoWindow: directions ${caseModel.lat} ${caseModel.lng}")
                tryOpenLocationOnMap(context, caseModel)
            }

            mInfoView.findViewById<View>(R.id.goToDetails).setOnClickListener {
                val intent = Intent(context, CaseDetailsActivity::class.java)
                intent.putExtra(CasesListFragment.EXTRA_CASE_MODEL, caseModel)
                context.startActivity(intent)
            }

        } catch (e: Exception) {
            Log.d(TAG, "getInfoWindow: error ${e.message}")
        }
        return mInfoView
    }

    @Throws(Exception::class)
    private fun setUserImage(profileImage: ImageView, imageUrl: String) {

        val circularProgressDrawable =
            CircularProgressDrawable(profileImage.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(profileImage.context)
            .load(imageUrl)
            .placeholder(circularProgressDrawable)
            .optionalTransform(CircleCrop())
            .error(R.drawable.ic_small_profile_image_place_holder)
            .into(profileImage)

    }

    fun getCaseLocationDistance(
        distanceProgressBar: View,
        distanceTextView: TextView,
        latitude: Double,
        longitude: Double
    ) {
        Log.d(TAG, "getCaseLocationDistance: $latitude $longitude")
        val body = LocationDistanceRequestBody(
            origin = Origin(
                lat = SessionConstants.myCurrentLocation!!.latitude,
                lng = SessionConstants.myCurrentLocation!!.longitude,
            ),
            destination = Destination(
                lat = latitude,
                lng = longitude,
            )
        )
        RetrofitClient.retrofitMap.getCaseDistance(body)
            .enqueue(object : Callback<LocationDistanceModel> {
                override fun onResponse(
                    call: Call<LocationDistanceModel>,
                    response: Response<LocationDistanceModel>
                ) {
                    if (response.isSuccessful) {
                        try {
                            response.body()?.let {
                                distanceTextView.text =
                                    it.routes[0].paths[0].distanceText!!.toString()
                            }
                        } catch (e: Exception) {

                        }
                    }
                    distanceProgressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<LocationDistanceModel>, t: Throwable) {
                    distanceProgressBar.visibility = View.GONE
                }
            })
    }


    private fun tryOpenLocationOnMap(context: Context, currentCase: CaseModel) {
        try {
            val uri = Uri.parse("geo:0,0?q=${currentCase.latitude},${currentCase.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                intent.setPackage("com.google.android.apps.maps")
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            showDownloadMapDialog(context, currentCase)
        }
    }

    private fun showDownloadMapDialog(context: Context, currentCase: CaseModel) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.maps_not_found_msg))
        builder.setPositiveButton(context.getString(R.string.downlad_map_app)) { dialog, which ->
            startHuaweiAppGallery(context)
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(R.string.open_on_web)) { dialog, which ->
            openWebPage(context, currentCase)
            dialog.dismiss()
        }
        builder.show()
    }

    private fun startHuaweiAppGallery(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://appgallery.huawei.com/app/C102457337")
        )
        context.startActivity(intent)
    }

    fun openWebPage(context: Context, currentCase: CaseModel) {
        val url =
            "https://www.google.com/maps/search/?api=1&query=${currentCase.latitude},${currentCase.longitude}"
        val builder = CustomTabsIntent.Builder()
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(context.resources.getColor(R.color.colorPrimary))
            .build()
        builder.setDefaultColorSchemeParams(defaultColors)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }


    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}