package com.bluethunder.tar2.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentNewPasswordBinding
import com.bluethunder.tar2.ui.BaseFragment

class NewPasswordFragment : BaseFragment() {


    private lateinit var binding: FragmentNewPasswordBinding

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

        initViews()
    }

    private fun initViews() {
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.resendCodeLayout.setOnClickListener {
            requireActivity().onBackPressed()
        }


    }
}