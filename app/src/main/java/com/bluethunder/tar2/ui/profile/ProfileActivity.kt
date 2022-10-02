package com.bluethunder.tar2.ui.profile

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ActivityProfileBinding
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.profile.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.github.dhaval2404.imagepicker.ImagePicker
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
        viewModel.listenToUserDetails()
        viewModel.currentCaseUserDetails.observe(this) { userData ->
            userData?.let {
                setUpUserData(it)
            }
        }
        viewModel.uploadingImage.observe(this) { reource ->
            binding.profileProgress.visibility = if (reource) View.VISIBLE else View.GONE
            binding.icEditProfile.visibility = if (reource) View.GONE else View.VISIBLE
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
        binding.editProfileView.setOnClickListener {
            pickImage()
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            saveUserName()
        }
    }

    private fun saveUserName() {
        val name = binding.nameInput.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameInput.error = getString(R.string.enter_name_err_msg)
            binding.nameInput.showSnakeBarError(getString(R.string.enter_name_err_msg))
            return
        }
        viewModel.updateUserName(name)
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

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            try {
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        val fileUri = data?.data!!
                        viewModel.uploadProfileImage(fileUri.path!!)
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            this, ImagePicker.getError(data), Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {

                    }
                }
            } catch (e: Exception) {
            }

        }


    private fun pickImage() {
        ImagePicker.with(this).crop().compress(512).maxResultSize(
            1080, 1080
        ).createIntent { intent ->
            startForProfileImageResult.launch(intent)
        }
    }

    override fun onDestroy() {
        viewModel.removeSnapshotListener()
        super.onDestroy()
    }
}