package com.bluethunder.tar2.ui.auth.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentRegisterBinding
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.fragments.VerifyPhoneFragment.Companion.COUNTRY_CODE_KEY
import com.bluethunder.tar2.ui.auth.fragments.VerifyPhoneFragment.Companion.PHONE_NUMBER_KEY
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.agconnect.auth.SignInResult
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import me.ibrahimsn.lib.PhoneNumberKit


class RegisterFragment : BaseFragment() {

    companion object {
        private const val TAG = "RegisterFragment"
        const val COMPLETE_REGISTER_KEY = "complete_register_key"
        const val USER_MODEL_KEY = "user_model_key"
        const val REGISTER_TYPE_HUAWEI_ID = "register_type_huawei_id"
    }

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var phoneNumberKit: PhoneNumberKit
    private lateinit var progressDialog: Dialog

    private var isCompleteRegister = false
    private var registerFromHuaweiID = false

    private val huaweiIdActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
                    if (authAccountTask.isSuccessful) {
                        val authAccount = authAccountTask.result
                        Log.i(TAG, "accessToken:" + authAccount.accessToken)
                        val credential =
                            HwIdAuthProvider.credentialWithToken(authAccount.accessToken)
                        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
                            Log.d(TAG, "signInWithHuaweiId: success ${it.user.uid}")
                            viewModel.setSignInWithHuaweiIdResponse(Resource.success(it))
                        }.addOnFailureListener {
                            Log.d(TAG, "signInWithHuaweiId: message =  ${it.message}")
                            Log.d(
                                TAG,
                                "signInWithHuaweiId: localizedMessage =  ${it.localizedMessage}"
                            )
                            viewModel.setSignInWithHuaweiIdResponse(Resource.error(it.message))
                        }.addOnCanceledListener {
                            Log.d(TAG, "signInWithHuaweiId: message = canceled")
                            viewModel.setSignInWithHuaweiIdResponse(Resource.error("canceled"))
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "signInWithHuaweiId: message = canceled")
                    viewModel.setSignInWithHuaweiIdResponse(Resource.error("canceled"))
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasInitializedRootView) {
            binding.root
        } else {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
            binding.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasInitializedRootView) {
            observeToViewModel()
            return
        }
        hasInitializedRootView = true
        isCompleteRegister = arguments?.getBoolean(COMPLETE_REGISTER_KEY) ?: false

        initViewModel()
        initViews()
        if (!isCompleteRegister)
            requestReadContactsPermission()

    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        binding.viewmodel = viewModel
        observeToViewModel()
    }

    private fun observeToViewModel() {
        viewModel.resetRegisterFields()
        viewModel.uploadingImage.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    verifyUserEmailAndPhone()
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    binding.profilePictureView.showSnakeBarError(resource.errorBody.toString())
                    progressDialog.dismiss()
                }
                else -> {}
            }
        }

        viewModel.signInWithHuaweiId.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    Log.d(TAG, "observeToViewModel:isCompleteRegister= ${isCompleteRegister}")
                    progressDialog.dismiss()
                    registerFromHuaweiID = true
                    completeCreateAccount(resource.data!!)
                }
                Status.ERROR -> {
                    progressDialog.dismiss()
                    Log.d(TAG, "signInWithHuaweiId: ${resource.errorBody.toString()}")
                    binding.createWithHuaweiIdBtn.showSnakeBarError(resource.errorBody.toString())
                }
                else -> {}
            }
        }
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        setUpPhoneNumberTextField()
        binding.createAccountBtn.setOnClickListener {
            validateAndRegister()
        }

        binding.imageViewContainer.setOnClickListener {
            pickImage()
        }
        binding.clearProfilePic.setOnClickListener {
            clearImage()
        }
        binding.termsAndConditionsContainer.setOnClickListener {
            showTermsAndConditions()
        }

        binding.createWithHuaweiIdBtn.setOnClickListener {
            crateAccountWithHuaweiID()
        }

        checkRegisterType()
    }

    private fun checkRegisterType() {
        if (isCompleteRegister) {
            binding.headerTv.text = getString(R.string.complete_register)
            binding.logoImg.visibility = View.GONE
            binding.subHeaderTv.visibility = View.GONE
            binding.loginWithHuaweiContainer.visibility = View.GONE
        }
    }

    private fun crateAccountWithHuaweiID() {
        try {
            AGConnectAuth.getInstance().signOut()
            viewModel.setSignInWithHuaweiIdResponse(Resource.loading())
            val authParams: AccountAuthParams =
                AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                    .setAccessToken()
                    .createParams()
            val service = AccountAuthManager.getService(requireActivity(), authParams)

            huaweiIdActivityLauncher.launch(service!!.signInIntent)
        } catch (e: Exception) {
            viewModel.signInWithHuaweiIdUnifiedMethod(requireActivity())
        }
//        completeCreateAccount()
    }

    private fun clearImage() {
        binding.profilePictureView.setImageResource(R.drawable.ic_place_holder)
        binding.clearProfilePic.visibility = View.GONE
        viewModel.removeImage()
    }

    private fun imageSelected(fileUri: Uri) {
        binding.clearProfilePic.visibility = View.VISIBLE
        binding.profilePictureView.setImageURI(fileUri)
        viewModel.setProfileImageLocalPath(fileUri.path!!)
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    imageSelected(fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireActivity(),
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                }
            }
        }


    private fun pickImage() {
        ImagePicker.with(this)
            .crop(1f, 1f)
            .compress(512)
            .maxResultSize(
                540,
                540
            )
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun setUpPhoneNumberTextField() {
        phoneNumberKit =
            PhoneNumberKit.Builder((requireActivity() as AuthActivity)).setIconEnabled(true).build()
        try {
            val tm =
                requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            phoneNumberKit.attachToInput(
                binding.phoneNumberInputLayout,
                if (tm.networkCountryIso.isNullOrEmpty()) "eg" else tm.networkCountryIso
            )
        } catch (e: Exception) {
            e.printStackTrace()
            phoneNumberKit.attachToInput(binding.phoneNumberInputLayout, "eg")
        }

        phoneNumberKit.setupCountryPicker(
            activity = (requireActivity() as AuthActivity),
            searchEnabled = true
        )
    }

    private fun validateAndRegister() {
        registerFromHuaweiID = false

        if (binding.nameInput.text.toString().isEmpty()) {
            binding.nameInput.error = getString(R.string.enter_name_err_msg)
            binding.nameInput.showSnakeBarError(getString(R.string.enter_name_err_msg))
            return
        }

        if (binding.phoneNumberInputLayout.editText?.text.toString()
                .isEmpty() || !phoneNumberKit.isValid
        ) {
            binding.phoneNumberInputLayout.error = getString(R.string.enter_phone_err_msg)
            binding.phoneNumberInputLayout.showSnakeBarError(getString(R.string.enter_phone_err_msg))
            return
        }

        if (binding.passwordInput.text.toString()
                .isEmpty() || binding.passwordInput.text.toString().length < 6
        ) {
            binding.passwordInput.error = getString(R.string.enter_password_err_msg)
            binding.passwordInput.showSnakeBarError(getString(R.string.enter_password_err_msg))
            return
        }

        if (viewModel.isImageSelected()) {
            if (viewModel.imageUploaded) {
                verifyUserEmailAndPhone()
            } else {
                viewModel.uploadProfileImage()
            }
        } else {
            verifyUserEmailAndPhone()
        }

    }

    private fun verifyUserEmailAndPhone() {
        val userModel = getUserModelFromFields()

        val phone = phoneNumberKit.parsePhoneNumber(
            binding.phoneNumberInputLayout.editText?.text.toString(),
            "eg"
        )
        phone?.let { phone ->
            try {
                NavHostFragment.findNavController(this)
                    .navigate(
                        R.id.action_registerFragment_to_verifyPhoneFragment,
                        bundleOf(
                            USER_MODEL_KEY to userModel,
                            REGISTER_TYPE_HUAWEI_ID to isCompleteRegister,
                            PHONE_NUMBER_KEY to phone.nationalNumber.toString(),
                            COUNTRY_CODE_KEY to phone.countryCode.toString()
                        )

                    )
            } catch (e: Exception) {
                Log.d(TAG, "completeCreateAccount:Exception $e")
            }
        } ?: run {
            binding.createAccountBtn.showSnakeBarError(getString(R.string.enter_phone_err_msg))
        }

    }

    private fun completeCreateAccount(signInResult: SignInResult) {

        val userModel = getUserModelFromHuaweiID(signInResult)
        Log.d(TAG, "completeCreateAccount: $userModel")

        try {
            NavHostFragment.findNavController(this)
                .navigate(
                    R.id.action_complete_registerFragment,
                    bundleOf(COMPLETE_REGISTER_KEY to true, USER_MODEL_KEY to userModel)
                )
        } catch (e: Exception) {
            Log.d(TAG, "completeCreateAccount:Exception $e")
        }

    }

    private fun getUserModelFromHuaweiID(signInResult: SignInResult): UserModel {
        val userModel = UserModel()
        userModel.id = signInResult.user.uid
        userModel.name = signInResult.user.displayName
        userModel.phone = signInResult.user.phone
        userModel.password = signInResult.user.uid
        userModel.imageUrl = signInResult.user.photoUrl
        return userModel
    }

    private fun getUserModelFromFields(): UserModel {
        val userModel = UserModel()
        val phone = phoneNumberKit.parsePhoneNumber(
            binding.phoneNumberInputLayout.editText?.text.toString(),
            "eg"
        )
        userModel.name = binding.nameInput.text.toString()
        phone?.let { parsedPhone ->
            parsedPhone.countryCode?.let { countryCode ->
                userModel.countryCode = countryCode.toString()
            }
        }
        userModel.countryCode = phone!!.countryCode.toString()
        userModel.phone = binding.phoneNumberInput.text.toString()
            .replace(" ", "").trim()
        userModel.password = binding.passwordInput.text.toString()

        if (viewModel.profileImageUrl.isNotEmpty()) {
            userModel.imageUrl = viewModel.profileImageUrl
        }
        return userModel
    }

    private fun requestReadContactsPermission() {

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.terms_and_conditions))
            .setMessage(getString(R.string.by_registering_you_agree_to_our) + " " + getString(R.string.terms_and_conditions))
            .setNegativeButton(getString(R.string.agree)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.show_terms)) { dialog, which ->
                showTermsAndConditions()
            }.show()
    }

    private fun showTermsAndConditions() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/eslamfaisal/tar2-privacy-policy/blob/main/README.md")
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy: ")
    }

    override fun onDestroyView() {
        removeObservers()
        super.onDestroyView()

    }

    private fun removeObservers() {
        Log.d(TAG, "removeObservers: ")
        viewModel.uploadingImage.removeObservers(viewLifecycleOwner)
        viewModel.signInWithHuaweiId.removeObservers(viewLifecycleOwner)
    }
}