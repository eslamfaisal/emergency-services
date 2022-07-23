package com.bluethunder.tar2.ui.edit_case.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.databinding.FragmentCasePersonalDataBinding
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog

class CasePersonalDataFragment : BaseFragment() {


    private lateinit var binding: FragmentCasePersonalDataBinding
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
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.fragment_case_personal_data,
                    container,
                    false
                )
            binding.root
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initViewModel()
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()

        binding.showPersonalDataBox.setOnClickListener {
            handleShowPersonalData()
        }
        binding.phoneNumberInput.setText(SessionConstants.currentLoggedInUserModel!!.phone!!)
        binding.nameInput.setText(SessionConstants.currentLoggedInUserModel!!.name!!)

        binding.contactMeViaPhoneView.setOnClickListener {
            handleCallViaPHoneNumber()
        }

        binding.contactMeViaOnlineCallView.setOnClickListener {
            handleCallViaOnlineCall()
        }

        binding.contactMeViaVideoCallView.setOnClickListener {
            handleCallViaVideoCall()
        }
    }

    private fun handleCallViaVideoCall() {
        viewModel.handleCallViaVideoCall()
        binding.contactMeViaPhone.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                if (viewModel.currentCaseModel.value!!.hasPhoneCall)
                    R.drawable.ic_selected_check_box
                else R.drawable.ic_unselected_check_box
            )
        )
    }

    private fun handleCallViaPHoneNumber() {
        viewModel.handleCaseCallViewPhoneNumber()
        binding.contactMeViaPhone.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                if (viewModel.currentCaseModel.value!!.hasPhoneCall)
                    R.drawable.ic_selected_check_box
                else R.drawable.ic_unselected_check_box
            )
        )
    }

    private fun handleCallViaOnlineCall() {
        viewModel.handleCallViaOnlineCall()
        binding.contactMeViaOnlineCall.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                if (viewModel.currentCaseModel.value!!.hasPhoneCall)
                    R.drawable.ic_selected_check_box
                else R.drawable.ic_unselected_check_box
            )
        )
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as EditCaseActivity).viewModel
        binding.viewmodel = viewModel

        initObserveCaseDetails()
    }

    private fun initObserveCaseDetails() {
        viewModel.currentCaseModel.observe(viewLifecycleOwner) { caseModel ->
            Log.d(TAG, "initObservers: caseModel $caseModel")
            initShowUSerData(caseModel.showUserData)
        }
    }

    private fun initShowUSerData(showUserData: Boolean) {
        binding.personalDataLayout.visibility = if (showUserData) View.VISIBLE else View.GONE
        binding.personalDataCheckbox.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                if (showUserData) R.drawable.ic_selected_check_box
                else R.drawable.ic_unselected_check_box
            )
        )
    }

    fun handleShowPersonalData() {
        viewModel.reverseShowPersonalData()
    }

    companion object {
        private const val TAG = "CasePersonalDataFragmen"
    }


}