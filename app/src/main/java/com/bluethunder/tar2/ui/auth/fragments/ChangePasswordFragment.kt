package com.bluethunder.tar2.ui.auth.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentChangePasswordBinding
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.fragments.VerifyPhoneFragment.Companion.COUNTRY_CODE_KEY
import com.bluethunder.tar2.ui.auth.fragments.VerifyPhoneFragment.Companion.PHONE_NUMBER_KEY
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.profile.ChangePasswordActivity
import com.bluethunder.tar2.ui.profile.viewmodel.ChangePasswordViewModel
import com.bluethunder.tar2.utils.getErrorMsg
import com.huawei.agconnect.auth.VerifyCodeSettings
import me.ibrahimsn.lib.PhoneNumberKit
import me.ibrahimsn.lib.api.Phone


class ChangePasswordFragment : BaseFragment() {

    companion object {
        private const val TAG = "ChangePasswordFragment"
    }

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel
    private lateinit var phoneNumberKit: PhoneNumberKit
    private lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasInitializedRootView) {
            binding.root
        } else {
            binding =
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.fragment_change_password,
                    container,
                    false
                )
            binding.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        initViews()
    }


    private fun initViewModel() {
        viewModel = (requireActivity() as ChangePasswordActivity).viewModel
        binding.viewmodel = viewModel
        observeToViewModel()
    }

    private fun observeToViewModel() {
        viewModel.phoneCodeResult.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    viewModel.setPhoneCodeResult(Resource.empty())
                    goToNewPasswordFragment()
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "phoneCodeResult: ${resource.errorBody.toString()}")
                    parseErrorCodeBody(resource.errorBody.toString())
                    progressDialog.dismiss()
                }
                Status.EMPTY -> {}
            }
        }
    }

    private fun goToNewPasswordFragment() {
        validatePhoneNumber { phone ->
            try {
                NavHostFragment.findNavController(this)
                    .navigate(
                        R.id.action_changePasswordFragment_to_newPasswordFragment,
                        bundleOf(
                            PHONE_NUMBER_KEY to phone.nationalNumber.toString(),
                            COUNTRY_CODE_KEY to phone.countryCode.toString()
                        )
                    )
            } catch (e: Exception) {

            }
        }
    }


    private fun parseErrorCodeBody(errorBody: String) {
        binding.btnNext.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        setUpPhoneNumberTextField()

        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }

        binding.btnNext.setOnClickListener {
            validatePhoneNumber()
        }
    }

    private fun validatePhoneNumber() {
        if (binding.phoneNumberInputLayout.editText?.text.toString()
                .isEmpty() || !phoneNumberKit.isValid
        ) {
            binding.phoneNumberInputLayout.error = getString(R.string.enter_phone_err_msg)
            binding.phoneNumberInputLayout.showSnakeBarError(getString(R.string.enter_phone_err_msg))
            return
        }

        validatePhoneNumber { phone ->
            viewModel.verifyPhoneNumber(
                phone.countryCode.toString(),
                phone.nationalNumber.toString(),
                action = VerifyCodeSettings.ACTION_RESET_PASSWORD
            )
        }

    }

    private fun validatePhoneNumber(complete: (Phone) -> Unit) {

        val phone = phoneNumberKit.parsePhoneNumber(
            binding.phoneNumberInputLayout.editText?.text.toString(),
            "eg"
        )
        phone?.let { phone ->
            complete(phone)
        } ?: run {
            binding.btnNext.showSnakeBarError(getString(R.string.enter_phone_err_msg))
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


}