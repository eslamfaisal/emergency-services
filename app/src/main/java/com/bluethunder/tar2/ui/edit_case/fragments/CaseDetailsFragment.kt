package com.bluethunder.tar2.ui.edit_case.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants.currentLanguage
import com.bluethunder.tar2.databinding.FragmentCaseDetailsBinding
import com.bluethunder.tar2.model.Status
import com.bluethunder.tar2.ui.BaseFragment
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.extentions.showSnakeBarError
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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


    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.caseCategoryInput.setOnClickListener {
            serviceHistoryDialog?.show()
        }

        binding.caseLocationInput.setOnClickListener {
            viewModel.checkDeviceLocation(requireActivity())
        }

        binding.imageViewContainer.setOnClickListener {
            pickImage()
        }

        binding.clearProfilePic.setOnClickListener {
            removeImage()
        }

        binding.btnNext.setOnClickListener {
            validateCaseData()
        }

    }

    private fun validateCaseData() {

        if (!viewModel.isImageSelected())
            binding.caseTitleInput.showSnakeBarError(getString(R.string.select_iameg))

        if (binding.caseTitleInput.text.toString().isEmpty()) {
            binding.caseTitleInput.error = getString(R.string.case_title_err_msg)
            binding.caseTitleInput.showSnakeBarError(getString(R.string.case_title_err_msg))
            return
        }
        if (binding.caseCategoryInput.text.toString().isEmpty()) {
            binding.caseCategoryInput.error = getString(R.string.choose_category_err_msg)
            binding.caseCategoryInput.showSnakeBarError(getString(R.string.choose_category_err_msg))
            return
        }
        if (binding.caseLocationInput.text.toString().isEmpty()) {
            binding.caseLocationInput.error = getString(R.string.location_err_msg)
            binding.caseCategoryInput.showSnakeBarError(getString(R.string.choose_category_err_msg))
            return
        }

        if (binding.caseDescriptionInput.text.toString().isEmpty()) {
            binding.caseDescriptionInput.error = getString(R.string.enter_description)
            binding.caseDescriptionInput.showSnakeBarError(getString(R.string.choose_category_err_msg))
            return
        }

        moveToPersonalDataPage()
    }

    private fun moveToPersonalDataPage() {

    }

    private fun removeImage() {
        binding.mainImagePlaceholderView.visibility = View.VISIBLE
        binding.clearProfilePic.visibility = View.GONE
        binding.mainImageView.visibility = View.GONE
        viewModel.removeImage()
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as EditCaseActivity).viewModel
        binding.viewmodel = viewModel

        viewModel.getCategories()
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

        viewModel.categories.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    progressDialog.dismiss()
                    Log.d("EditCaseActivity ", "Categories : ${resource.data}")
                    resource.data?.let {
                        setUpCategoriesDialog(it)
                    } ?: run {
                        Log.d(TAG, "initObservers: categories is null")
                    }

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

    var selectedCategoryModel: CaseCategoryModel? = null
    var serviceHistoryDialog: AlertDialog? = null
    private fun setUpCategoriesDialog(data: MutableList<CaseCategoryModel>) {
        val historyOptions = data.map { if (currentLanguage == "ar") it.nameAr else it.nameEn }
            .toMutableList()

        val checkedItem = -1
        serviceHistoryDialog = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.select_category))
            .setPositiveButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(historyOptions.toTypedArray(), checkedItem) { dialog, which ->
                selectedCategoryModel = data[which]
                binding.caseCategoryInput.setText(historyOptions[which])
                dialog.dismiss()
            }.create()
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    imageSelected(fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireActivity(),
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                }
            }
        }


    private fun pickImage() {
        ImagePicker.with(this)
            .crop()
            .compress(512)
            .maxResultSize(
                1080,
                1080
            )
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }

    private fun imageSelected(fileUri: Uri) {
        binding.clearProfilePic.visibility = View.VISIBLE
        binding.mainImageView.visibility = View.VISIBLE
        binding.mainImageView.setImageURI(fileUri)
        viewModel.setProfileImageLocalPath(fileUri.path!!)
    }


    companion object {
        private const val TAG = "CaseDetailsFragment"
    }
}