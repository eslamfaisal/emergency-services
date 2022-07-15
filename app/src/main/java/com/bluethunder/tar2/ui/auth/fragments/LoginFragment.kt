package com.bluethunder.tar2.ui.auth.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentLoginBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.huawei.agconnect.auth.AGConnectAuth

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var binding: FragmentLoginBinding
    lateinit var viewModel: AuthViewModel
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
        initViewModel()
        initView()
        AGConnectAuth.getInstance().signOut()
    }

    private fun initView() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.btnNext.setOnClickListener {
            validateAndTryLogin()
        }

        binding.registerNewUserBtn.setOnClickListener {
            findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateAndTryLogin() {
        progressDialog.show()
        viewModel.loginWithEmailAndPassword(
            binding.emailInput.text.toString(),
            binding.passwordInput.text.toString()
        )
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        binding.viewmodel = viewModel

        viewModel.userData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.SUCCESS -> {
                    Log.d(TAG, "initViewModel: ${resource.data}")
                    progressDialog.dismiss()
                }
                Status.ERROR -> {
                    Log.d(TAG, "initViewModel: ${resource.errorBody}")
                    progressDialog.dismiss()
                }
            }
        }
    }


}