package com.bluethunder.tar2.ui.home.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentMyCasesBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.home.adapter.MyCasesAdapter
import com.bluethunder.tar2.ui.home.viewmodel.MyCasesViewModel
import com.bluethunder.tar2.utils.getErrorMsg


class MyCasesFragment : BaseFragment(), MyCasesAdapter.MyCasesInteractions {

    private val viewModel by viewModels<MyCasesViewModel> { getViewModelFactory() }
    private lateinit var binding: FragmentMyCasesBinding
    lateinit var progressDialog: Dialog

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel: get my cases")
        viewModel.myCases.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    myCasesAdapter.addNewData(resource.data!!)
                }
                Status.ERROR -> {
                    progressDialog.dismiss()
                    parsingError(resource.errorBody!!.toString())
                }
                Status.LOADING -> {
                    progressDialog.show()
                }
                Status.EMPTY -> {
                    progressDialog.dismiss()
                    myCasesAdapter.clearData()
                }
            }
        }
    }

    lateinit var myCasesAdapter: MyCasesAdapter
    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        myCasesAdapter = MyCasesAdapter(this)
        binding.myCasesRecyclerView.adapter = myCasesAdapter

    }

    private fun parsingError(errorBody: String) {
        binding.myCasesRecyclerView.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    override fun onNotificationClicked(caseModel: CaseModel) {


    }

    companion object {
        private const val TAG = "MyCasesFragment"
    }
}