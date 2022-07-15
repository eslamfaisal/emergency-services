package com.bluethunder.tar2.ui.auth.fragments

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentRegisterBinding
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import me.ibrahimsn.lib.PhoneNumberKit


class RegisterFragment : Fragment() {


    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: AuthViewModel
    lateinit var phoneNumberKit: PhoneNumberKit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_register, container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        binding.viewmodel = viewModel
    }

    private fun initViews() {
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        setUpPhoneNumberTextField()
        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }
    }

    fun setUpPhoneNumberTextField() {
        phoneNumberKit = PhoneNumberKit.Builder(requireActivity()).setIconEnabled(true).build()
        phoneNumberKit.setupCountryPicker(
            activity = (requireActivity() as AuthActivity),
            searchEnabled = true
        )
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

    }

    private fun validateAndRegister() {
        if (binding.nameInput.text.toString().isEmpty()) {
            binding.nameInput.error = getString(R.string.enter_name_err_msg)
            binding.nameInput.showSnakeBarError(getString(R.string.enter_name_err_msg))
            return
        }

        if (binding.emailInput.text.toString()
                .isEmpty() || !isValidEmail(binding.emailInput.text.toString())
        ) {
            binding.emailInput.error = getString(R.string.enter_email_err_msg)
            binding.emailInput.showSnakeBarError(getString(R.string.enter_email_err_msg))
            return
        }

        if (binding.passwordInput.text.toString().isEmpty()) {
            binding.passwordInput.error = getString(R.string.enter_password_err_msg)
            binding.passwordInput.showSnakeBarError(getString(R.string.enter_password_err_msg))
            return
        }


        if (binding.phoneNumberInputLayout.editText?.text.toString()
                .isEmpty() || !phoneNumberKit.isValid
        ) {
            binding.phoneNumberInputLayout.error = getString(R.string.enter_phone_err_msg)
            binding.phoneNumberInputLayout.showSnakeBarError(getString(R.string.enter_phone_err_msg))
            return
        }

    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

}