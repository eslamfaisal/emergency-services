package com.bluethunder.tar2.ui.auth.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentVerifyPhoneBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.fragments.RegisterFragment.Companion.REGISTER_TYPE_HUAWEI_ID
import com.bluethunder.tar2.ui.auth.fragments.RegisterFragment.Companion.USER_MODEL_KEY
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.home.MainActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys.IS_LOGGED_IN
import com.bluethunder.tar2.utils.SharedHelperKeys.USER_DATA
import com.bluethunder.tar2.utils.getErrorMsg
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson


class VerifyPhoneFragment : BaseFragment() {

    companion object {
        private const val TAG = "VerifyPhoneFragment"

        const val COUNTRY_CODE_KEY = "country_code_key"
        const val PHONE_NUMBER_KEY = "phone_number_key"
        const val FULL_PHONE_NUMBER_KEY = "full_phone_number_key"
    }

    private lateinit var viewModel: AuthViewModel
    private lateinit var binding: FragmentVerifyPhoneBinding
    private lateinit var progressDialog: Dialog

    lateinit var userModel: UserModel
    lateinit var phoneNumber: String
    lateinit var countryCode: String

    private var registerFromHuaweiID = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasInitializedRootView) {
            binding.root
        } else {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_verify_phone, container, false)
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

        phoneNumber = arguments?.getString(PHONE_NUMBER_KEY)!!
        countryCode = arguments?.getString(COUNTRY_CODE_KEY)!!
        userModel = arguments?.getSerializable(USER_MODEL_KEY) as UserModel
        registerFromHuaweiID = arguments?.getBoolean(REGISTER_TYPE_HUAWEI_ID)!!

        initViewModel()
        initViews()

    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        binding.viewmodel = viewModel
        observeToViewModel()

        viewModel.verifyPhoneNumber(countryCode, phoneNumber)
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.phoneTv.text = "$countryCode $phoneNumber"

        binding.btnNext.setOnClickListener {
            createAccountWithPhone()
        }
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.resendCodeLayout.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

//    private fun sendCodeForResetPassword() {
//        viewModel.resetPassword(countryCode, phoneNumber)
//    }

    private fun observeToViewModel() {
        viewModel.phoneCodeResult.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "phoneCodeResult: ${resource.errorBody.toString()}")
                    binding.phoneTv.showSnakeBarError(resource.errorBody.toString())
                    progressDialog.dismiss()
                }
                else -> {}
            }
        }

        viewModel.newAccountWithPhoneResult.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    onPhoneVerified()
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "newAccountWithPhoneResult: ${resource.errorBody.toString()}")
                    parseErrorCodeBody(resource.errorBody.toString())
                    progressDialog.dismiss()
                }
                else -> {}
            }
        }

        viewModel.createUserData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    goToHome(resource.data!!)
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "createUserData: ${resource.errorBody.toString()}")
                    parseErrorCodeBody(resource.errorBody.toString())
                    progressDialog.dismiss()
                }
                else -> {}
            }
        }
    }

    fun goToHome(data: UserModel) {
        Toast.makeText(requireContext(), getString(R.string.register_succ_msg), Toast.LENGTH_LONG)
            .show()
        val userDataJson = Gson().toJson(data)
        SharedHelper.putBoolean(requireContext(), IS_LOGGED_IN, true)
        SharedHelper.putString(requireContext(), USER_DATA, userDataJson)
        goToLoginActivity()
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun onPhoneVerified() {
        createUserToDatabase()
    }

    private fun createUserToDatabase() {
        viewModel.createUserToDatabase(userModel)
    }

    private fun parseErrorCodeBody(errorBody: String) {
        if (errorBody.contains("203818038")) {
            showRegisteredBeforeAccountDialog()
        } else
            binding.phoneTv.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    private fun showRegisteredBeforeAccountDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.phone_regestered_before_mss))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
                requireActivity().onBackPressed()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    private fun createAccountWithPhone() {

        if (registerFromHuaweiID) {
            viewModel.linkPhoneToHuaweiIDAccount(
                countryCode,
                phoneNumber,
                userModel.password,
                binding.otpInput.text.toString()
            )
        } else {

            viewModel.createAccountWithPHoneNumber(
                countryCode,
                phoneNumber,
                userModel.password,
                binding.otpInput.text.toString()
            )
        }
    }

    override fun onDestroyView() {
        removeObservers()
        super.onDestroyView()
    }

    private fun removeObservers() {
        viewModel.phoneCodeResult.removeObservers(viewLifecycleOwner)
    }
}