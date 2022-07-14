package com.bluethunder.tar2.ui.auth.fragments

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

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private lateinit var viewDataBinding: FragmentLoginBinding

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
        viewDataBinding.btnNext.setOnClickListener {
            requireActivity().showLoadingDialog()
        }
    }

    private fun initViewModel() {
        val viewModel = (requireActivity() as AuthActivity).viewModel
        viewDataBinding.viewmodel = viewModel

        viewModel.setOnMapSelected(1)
        viewModel.onSelectedTabIndex.observe(viewLifecycleOwner) {
            Log.d(TAG, "initViewModel: $it")
        }
    }


}