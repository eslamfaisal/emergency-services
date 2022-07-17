package com.bluethunder.tar2.ui.auth.fragments

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentChangePasswordBinding
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.fragments.VerifyPhoneFragment.Companion.COUNTRY_CODE_KEY
import com.bluethunder.tar2.ui.auth.fragments.VerifyPhoneFragment.Companion.PHONE_NUMBER_KEY
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import me.ibrahimsn.lib.PhoneNumberKit


class ChangePasswordFragment : BaseFragment() {


    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var phoneNumberKit: PhoneNumberKit

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

        if (hasInitializedRootView) return
        hasInitializedRootView = true

        initViews()
    }

    private fun initViews() {

        setUpPhoneNumberTextField()

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
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
        verifyUserEmailAndPhone()
    }

    private fun verifyUserEmailAndPhone() {

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
                            PHONE_NUMBER_KEY to phone.nationalNumber.toString(),
                            COUNTRY_CODE_KEY to phone.countryCode.toString()
                        )
                    )
            } catch (e: Exception) {

            }
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