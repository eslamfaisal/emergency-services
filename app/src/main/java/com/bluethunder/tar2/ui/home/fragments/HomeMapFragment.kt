package com.bluethunder.tar2.ui.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentHomeMapBinding
import com.bluethunder.tar2.ui.getViewModelFactory
import com.bluethunder.tar2.ui.home.viewmodel.MapScreenViewModel


class HomeMapFragment : Fragment() {

    private val viewModel by viewModels<MapScreenViewModel> { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentHomeMapBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewDataBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home_map, container,
            false
        )
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}