package com.bluethunder.tar2.ui.auth.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentLoginBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.showLoadingDialog
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.EmailAuthProvider
import com.huawei.agconnect.auth.EmailUser
import com.huawei.agconnect.auth.VerifyCodeSettings

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var viewDataBinding: FragmentLoginBinding
    lateinit var viewModel: AuthViewModel
    lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_login, container,
            false
        )
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
        AGConnectAuth.getInstance().signOut()
    }

    private fun initView() {
        progressDialog = requireActivity().showLoadingDialog()
        viewDataBinding.btnNext.setOnClickListener {
            validateAndTryLogin()
        }
    }

    private fun validateAndTryLogin() {
        progressDialog.show()
        viewModel.loginWithEmailAndPassword(
            viewDataBinding.emailInput.text.toString(),
            viewDataBinding.passwordInput.text.toString()
        )
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as AuthActivity).viewModel
        viewDataBinding.viewmodel = viewModel

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