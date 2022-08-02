package com.bluethunder.tar2.ui.home.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentHomeMapBinding
import com.bluethunder.tar2.databinding.FragmentMyCasesBinding
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.home.viewmodel.MapScreenViewModel
import com.bluethunder.tar2.ui.home.viewmodel.MyCasesViewModel


class MyCasesFragment : BaseFragment() {

    private val viewModel by viewModels<MyCasesViewModel> { getViewModelFactory() }
    private lateinit var binding: FragmentMyCasesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_my_cases, container,
            false
        )
        return binding.root
    }
}