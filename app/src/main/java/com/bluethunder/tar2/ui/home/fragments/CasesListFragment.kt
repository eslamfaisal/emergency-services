package com.bluethunder.tar2.ui.home.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentCasesListBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.bluethunder.tar2.ui.home.adapter.CasesListAdapter
import com.bluethunder.tar2.ui.home.adapter.CategoriesAdapter
import com.bluethunder.tar2.ui.home.viewmodel.CasesListViewModel
import com.bluethunder.tar2.utils.getErrorMsg
import com.bluethunder.tar2.views.setupRefreshLayout

class CasesListFragment : BaseFragment(), CasesListAdapter.CasesListInteractions,
    CategoriesAdapter.CategoryInteractions {

    private val viewModel by viewModels<CasesListViewModel> { getViewModelFactory() }
    private lateinit var binding: FragmentCasesListBinding
    lateinit var progressDialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_cases_list, container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner
        this.setupRefreshLayout(binding.refreshLayout)
        initViews()
        initViewModel()
    }

    lateinit var categoriesAdapter: CategoriesAdapter
    lateinit var myCasesAdapter: CasesListAdapter
    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()

        myCasesAdapter = CasesListAdapter(this)
        binding.casesListRecyclerView.apply {
            adapter = myCasesAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        categoriesAdapter = CategoriesAdapter(this)
        binding.categoryListRecyclerView.apply {
            adapter = categoriesAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        }

    }

    private fun initViewModel() {
        viewModel.getCategories()
        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    categoriesAdapter.addNewData(resource.data!!)
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
        viewModel.casesList.observe(viewLifecycleOwner) { resource ->
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

    private fun parsingError(errorBody: String) {
        binding.casesListRecyclerView.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    override fun onCasenClicked(caseModel: CaseModel) {

    }

    override fun onCategoryClicked(category: CaseCategoryModel) {
        viewModel.getCasesList(category)
    }
}