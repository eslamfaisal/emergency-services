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
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.showLoadingDialog
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.EmailAuthProvider

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var viewDataBinding: FragmentLoginBinding
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
    }

    private fun initView() {
        progressDialog = requireActivity().showLoadingDialog()
        viewDataBinding.btnNext.setOnClickListener {
            progressDialog.show()
            val credential = EmailAuthProvider.credentialWithPassword("eslam.faisal.ef@gmail.com", "password")
            AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
                // Obtain sign-in information.
                Log.d(TAG, "initView: ${it.user.displayName}")
                progressDialog.dismiss()
            }.addOnFailureListener {
                // onFail
                Log.d(TAG, "initView: ${it.message}")
                progressDialog.dismiss()
            }

        }
    }

    private fun initViewModel() {
        val viewModel = (requireActivity() as AuthActivity).viewModel
        viewDataBinding.viewmodel = viewModel

        viewModel.getUserDetails()
        viewModel.userData.observe(viewLifecycleOwner) {
            Log.d(TAG, "initViewModel: $it")
        }
    }


}