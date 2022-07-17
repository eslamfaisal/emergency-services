package com.bluethunder.tar2.ui.auth.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.bluethunder.tar2.utils.getErrorMsg


class VerifyPhoneFragment : BaseFragment() {

    companion object {
        private const val TAG = "VerifyPhoneFragment"

        const val COUNTRY_CODE_KEY = "country_code_key"
        const val PHONE_NUMBER_KEY = "phone_number_key"
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

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        viewModel.verifyPhoneNumber(countryCode, phoneNumber)
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.phoneTv.text = "$countryCode $phoneNumber"
        binding.btnNext.setOnClickListener {
            createAccountWithPhone()
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
                    onPhoneVerified(resource.data!!)
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

    private fun goToHome(data: UserModel) {
        Log.d(TAG, "goToHome: $data")
        val intent = Intent(requireActivity(), MainActivity::class.java)
        requireActivity().startActivity(intent)
    }

    private fun onPhoneVerified(msg: String) {
        binding.phoneTv.showSnakeBarError(msg)

        createUserToDatabase()
    }

    private fun createUserToDatabase() {
        viewModel.createUserToDatabase(userModel)
    }

    private fun parseErrorCodeBody(errorBody: String) {
        binding.phoneTv.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
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