package com.bluethunder.tar2.ui.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.databinding.ActivityProfileBinding
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.profile.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class ProfileActivity : AppCompatActivity() {


    val viewModel by viewModels<ProfileViewModel> { getViewModelFactory() }

    private lateinit var binding: ActivityProfileBinding

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

    }


}