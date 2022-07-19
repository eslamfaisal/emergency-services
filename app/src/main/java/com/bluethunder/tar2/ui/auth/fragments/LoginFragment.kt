package com.bluethunder.tar2.ui.auth.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentLoginBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.home.MainActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.bluethunder.tar2.utils.getErrorMsg
import com.google.gson.Gson
import com.huawei.agconnect.auth.AGConnectAuth
import me.ibrahimsn.lib.PhoneNumberKit

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: AuthViewModel
    private lateinit var phoneNumberKit: PhoneNumberKit

    lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_login, container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AGConnectAuth.getInstance().signOut()
        initViewModel()
        initView()
        AGConnectAuth.getInstance().signOut()
    }

    private fun initView() {
        progressDialog = requireActivity().showLoadingDialog()
        setUpPhoneNumberTextField()

        binding.btnNext.setOnClickListener {
            validateAndTryLogin()
        }

        binding.registerNewUserBtn.setOnClickListener {
            try {
                findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment)
            } catch (e: Exception) {
            }
        }

        binding.huaweiIdSignInBtn.setOnClickListener {
            viewModel.signInWithHuaweiId(requireActivity())
        }
        binding.forgetPassword.setOnClickListener {
            try {
                findNavController(this).navigate(R.id.action_loginFragment_to_changePasswordFragment)
            } catch (e: Exception) {
            }
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

    private fun validateAndTryLogin() {
        AGConnectAuth.getInstance().signOut()

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

        val phone = phoneNumberKit.parsePhoneNumber(
            binding.phoneNumberInputLayout.editText?.text.toString(),
            "eg"
        )
        viewModel.loginWithEmailAndPassword(
            phone!!.countryCode.toString(),
            phone.nationalNumber.toString(),
            binding.passwordInput.text.toString()
        )

    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        binding.viewmodel = viewModel

        viewModel.signInWithHPhone.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    viewModel.getUserDetails(resource.data!!)
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "initViewModel: ${resource.errorBody}")
                    parsingError(resource.errorBody.toString())
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
                    Log.d(TAG, "initViewModel: ${resource.data!!.user.phone}")
                    viewModel.getUserDetails(resource.data!!)
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "initViewModel: ${resource.errorBody}")
                    parsingError(resource.errorBody.toString())
                    progressDialog.dismiss()
                }
                else -> {}
            }
        }

        viewModel.getUserData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    goToHome(resource.data!!)
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    parsingError(resource.errorBody.toString())
                    Log.d(TAG, "initViewModel: ${resource.errorBody}")
                    progressDialog.dismiss()
                }
                else -> {}
            }
        }

    }

    private fun parsingError(errorBody: String) {
        binding.btnNext.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    fun goToHome(data: UserModel) {
        val userDataJson = Gson().toJson(data)
        SharedHelper.putBoolean(requireContext(), SharedHelperKeys.IS_LOGGED_IN, true)
        SharedHelper.putString(requireContext(), SharedHelperKeys.USER_DATA, userDataJson)
        goToLoginActivity()
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }
}