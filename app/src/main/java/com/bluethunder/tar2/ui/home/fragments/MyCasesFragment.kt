package com.bluethunder.tar2.ui.home.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentMyCasesBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.case_details.CaseDetailsActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.home.adapter.MyCasesAdapter
import com.bluethunder.tar2.ui.home.viewmodel.MyCasesViewModel
import com.bluethunder.tar2.utils.getErrorMsg
import com.bluethunder.tar2.views.setupRefreshLayout


class MyCasesFragment : BaseFragment(), MyCasesAdapter.MyCasesInteractions {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
        requireActivity().setupRefreshLayout(binding.refreshLayout)
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        Log.d(TAG, "initViewModel: get my cases")
        viewModel.getMyCases()
        viewModel.myCases.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    myCasesAdapter.addNewData(resource.data!!)
                }
                Status.ERROR -> {
                    hideProgress()
                    parsingError(resource.errorBody!!.toString())
                }
                Status.LOADING -> {
                    showProgress()
                }
                Status.EMPTY -> {
                    hideProgress()
                    myCasesAdapter.clearData()
                }
            }
        }
    }

    private fun hideProgress() {
        binding.progressHorizontal.visibility = View.GONE
    }

    private fun showProgress() {
        binding.progressHorizontal.visibility = View.VISIBLE
    }

    lateinit var myCasesAdapter: MyCasesAdapter
    private fun initViews() {
        myCasesAdapter = MyCasesAdapter(this)
        binding.myCasesRecyclerView.apply {
            adapter = myCasesAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

    }

    private fun parsingError(errorBody: String) {
        binding.myCasesRecyclerView.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    override fun onMyCaseClicked(caseModel: CaseModel) {
        val intent = Intent(requireActivity(), CaseDetailsActivity::class.java)
        intent.putExtra(CasesListFragment.EXTRA_CASE_MODEL, caseModel)
        requireActivity().startActivity(intent)
    }

    companion object {
        private const val TAG = "MyCasesFragment"
    }
}