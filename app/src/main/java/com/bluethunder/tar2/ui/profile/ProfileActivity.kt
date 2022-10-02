package com.bluethunder.tar2.ui.profile

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.databinding.ActivityProfileBinding
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.profile.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import me.ibrahimsn.lib.PhoneNumberKit

class ProfileActivity : AppCompatActivity() {


    val viewModel by viewModels<ProfileViewModel> { getViewModelFactory() }

    private lateinit var binding: ActivityProfileBinding
    private lateinit var phoneNumberKit: PhoneNumberKit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        initView()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.listenToUserDetails(SessionConstants.currentLoggedInUserModel!!.id!!)
        viewModel.currentCaseUserDetails.observe(this) { userData ->
            userData?.let {
                setUpUserData(it)
            }
        }
    }

    private fun setUpUserData(userModel: UserModel) {

        userModel.name?.let {
            binding.nameInput.setText(it)
        }

        userModel.phone?.let {
            binding.phoneNumberInput.setText(it)
        }

        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(this).load(userModel.imageUrl).placeholder(circularProgressDrawable)
            .optionalTransform(CircleCrop()).error(R.drawable.ic_small_profile_image_place_holder)
            .into(binding.profileImage)
    }

    private fun initView() {
        setUpPhoneNumberTextField()
    }

    private fun setUpPhoneNumberTextField() {
        phoneNumberKit = PhoneNumberKit.Builder(this).setIconEnabled(true).build()

        try {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            phoneNumberKit.attachToInput(
                binding.phoneNumberInputLayout,
                if (tm.networkCountryIso.isNullOrEmpty()) "eg" else tm.networkCountryIso
            )
        } catch (e: Exception) {
            e.printStackTrace()
            phoneNumberKit.attachToInput(binding.phoneNumberInputLayout, "eg")
        }

        phoneNumberKit.setupCountryPicker(
            activity = this, searchEnabled = true
        )
    }

}