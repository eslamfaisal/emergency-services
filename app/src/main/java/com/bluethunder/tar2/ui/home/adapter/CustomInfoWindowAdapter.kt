package com.bluethunder.tar2.ui.home.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.networking.RetrofitClient
import com.bluethunder.tar2.ui.case_details.model.Destination
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceModel
import com.bluethunder.tar2.ui.case_details.model.LocationDistanceRequestBody
import com.bluethunder.tar2.ui.case_details.model.Origin
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.utils.TimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.model.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomInfoWindowAdapter(val context: Context) : HuaweiMap.InfoWindowAdapter {

    companion object {
        private const val TAG = "CustomInfoWindowAdapter"
    }

    override fun getInfoWindow(marker: Marker?): View {

        val mInfoView =
            (context as Activity).layoutInflater.inflate(R.layout.custom_info_window, null)
        try {
            Log.d(TAG, "getInfoWindow: ${marker?.title}")
            val mInfoWindow: CaseModel? = marker?.tag as CaseModel?
            val timeAgo = TimeAgo()
            timeAgo.locale(context)
            mInfoView.findViewById<TextView>(R.id.date_tv).text =
                timeAgo.getTimeAgo(mInfoWindow?.createdAt!!)
            mInfoView.findViewById<TextView>(R.id.username_tv).text = mInfoWindow.userName
            mInfoView.findViewById<TextView>(R.id.infoTile).text = mInfoWindow.title
            mInfoView.findViewById<TextView>(R.id.infoAddress).text = mInfoWindow.description
            setUserImage(
                mInfoView.findViewById(R.id.profile_image),
                mInfoWindow.userImage!!
            )
            getCaseLocationDistance(
                mInfoView.findViewById(R.id.distance_progress_bar),
                mInfoView.findViewById(R.id.distance_tv),
                mInfoWindow.lat,
                mInfoWindow.lng,
            )
        } catch (e: Exception) {
            Log.d(TAG, "getInfoWindow: ${e.message}")
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
        RetrofitClient.retrofit.getCaseDistance(body)
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

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}