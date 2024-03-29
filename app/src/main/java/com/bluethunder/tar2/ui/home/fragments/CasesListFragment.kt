package com.bluethunder.tar2.ui.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.FragmentCasesListBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.case_details.CaseDetailsActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
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
        requireActivity().setupRefreshLayout(binding.refreshLayout)
        initViews()
        initViewModel()
    }

    lateinit var categoriesAdapter: CategoriesAdapter
    lateinit var myCasesAdapter: CasesListAdapter
    private fun initViews() {

        myCasesAdapter = CasesListAdapter(this)
        binding.casesListRecyclerView.apply {
            adapter = myCasesAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }

        categoriesAdapter = CategoriesAdapter(this)
        binding.categoryListRecyclerView.apply {
            adapter = categoriesAdapter
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        }

        ViewCompat.setNestedScrollingEnabled(binding.casesListRecyclerView, false)
        ViewCompat.setNestedScrollingEnabled(binding.categoryListRecyclerView, false)

    }

    private fun initViewModel() {
        viewModel.getCategories()
        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    categoriesAdapter.addNewData(resource.data!!)
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
                    categoriesAdapter.clearData()
                }
            }
        }
        viewModel.addedCasesList.observe(viewLifecycleOwner) { resource ->
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
        viewModel.deletedCases.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    myCasesAdapter.addDeleteData(resource.data!!)
                }
                else -> {}
            }
        }
    }

    private fun hideProgress() {
        binding.progressHorizontal.visibility = View.GONE
    }

    private fun showProgress() {
        binding.progressHorizontal.visibility = View.VISIBLE
    }

    private fun parsingError(errorBody: String) {
        binding.casesListRecyclerView.showSnakeBarError(requireActivity().getErrorMsg(errorBody))
    }

    override fun onCaseClicked(caseModel: CaseModel) {
        val intent = Intent(requireActivity(), CaseDetailsActivity::class.java)
        intent.putExtra(EXTRA_CASE_MODEL, caseModel)
        requireActivity().startActivity(intent)
    }

    override fun onCategoryClicked(category: CaseCategoryModel) {
        viewModel.getCasesList(category)
    }

    companion object {
        const val EXTRA_CASE_MODEL = "extra_case_model"
    }
}