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
import com.bluethunder.tar2.databinding.FragmentNewPasswordBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.utils.getErrorMsg
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NewPasswordFragment : BaseFragment() {

    companion object {
        const val TAG = "NewPasswordFragment"
    }

    private lateinit var binding: FragmentNewPasswordBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var progressDialog: Dialog

    lateinit var phoneNumber: String
    lateinit var countryCode: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasInitializedRootView) {
            binding.root
        } else {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_new_password, container, false)
            binding.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phoneNumber = arguments?.getString(VerifyPhoneFragment.PHONE_NUMBER_KEY)!!
        countryCode = arguments?.getString(VerifyPhoneFragment.COUNTRY_CODE_KEY)!!

        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        binding.viewmodel = viewModel

        viewModel.resetPasswordResult.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    navigateToLogin()
                }
                Status.ERROR -> {
                    progressDialog.dismiss()
                    Log.d(TAG, "initViewModel: ${resource.errorBody}")
                    parseErrorCodeBody(resource.errorBody.toString())
                }
                else -> {}
            }
        }
    }

    fun navigateToLogin() {
        MaterialAlertDialogBuilder(requireActivity())
            .setCancelable(false)
            .setTitle(getString(R.string.new_password))
            .setMessage(getString(R.string.password_reset_success))
            .setPositiveButton(getString(R.string.login)) { dialog, which ->
                goToLoginActivity()
            }.show()
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.resendCodeLayout.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            validateCodeAndPasswords()
        }
    }

    private fun parseErrorCodeBody(errorBody: String) {
        binding.btnNext.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    private fun validateCodeAndPasswords() {

        if (binding.otpInput.text.toString().isEmpty()) {
            binding.otpInput.error = getString(R.string.enter_otp_code)
            binding.otpInput.showSnakeBarError(getString(R.string.enter_otp_code))
            return
        }

        if (binding.passwordInput.text.toString()
                .isEmpty() || binding.passwordInput.text.toString().length < 6
        ) {
            binding.passwordInput.error = getString(R.string.enter_password_err_msg)
            binding.passwordInput.showSnakeBarError(getString(R.string.enter_password_err_msg))
            return
        }

        if (binding.confirmPasswordInput.text.toString() != binding.passwordInput.text.toString()) {
            binding.confirmPasswordInput.error = getString(R.string.enter_confirm_password_err_msg)
            binding.confirmPasswordInput.showSnakeBarError(getString(R.string.enter_confirm_password_err_msg))
            return
        }

        viewModel.resetPassword(
            countryCode,
            phoneNumber,
            binding.passwordInput.text.toString(),
            binding.otpInput.text.toString(),
        )

    }
}