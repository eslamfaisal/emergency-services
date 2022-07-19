package com.bluethunder.tar2.ui.edit_case.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentCaseDetailsBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog

class CaseDetailsFragment : BaseFragment() {

    private lateinit var binding: FragmentCaseDetailsBinding
    lateinit var viewModel: EditCaseViewModel
    lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasInitializedRootView) {
            binding.root
        } else {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_case_details, container, false)
            binding.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as EditCaseActivity).viewModel
        binding.viewmodel = viewModel


        initObservers()
    }

    private fun initObservers() {
        viewModel.locationAddress.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    binding.caseLocationInput.setText(resource.data.toString())
                    Log.d("EditCaseActivity ", "Addressnae : ${resource.data}")
                }
                Status.LOADING -> {
                    progressDialog.show()
                    Log.e("EditCaseActivity", "Loading")
                }
                else -> {
                    progressDialog.dismiss()
                    Log.e("EditCaseActivity", "Unknown error")
                }
            }
        }
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.caseCategoryInput.setOnClickListener {
            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.caseLocationInput.setOnClickListener {
            viewModel.checkDeviceLocation(requireActivity())
        }
    }


    companion object
}