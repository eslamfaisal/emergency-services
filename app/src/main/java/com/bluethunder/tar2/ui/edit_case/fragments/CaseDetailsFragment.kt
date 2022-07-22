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
import androidx.navigation.fragment.findNavController
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
import com.bumptech.glide.Glide
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

        if (!viewModel.isImageSelected()) {
            binding.caseTitleInput.showSnakeBarError(getString(R.string.select_iameg))
            return
        }

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
        try {
            findNavController().navigate(
                R.id.action_caseDetailsFragment_to_casePersonalDataFragment
            )
            viewModel.setSelectedFragmentIndex(1)
        } catch (e: Exception) {
        }

    }

    private fun removeImage() {
        showMainImagePlaceHolder()
        viewModel.removeImage()
    }

    private fun initViewModel() {
        viewModel = (requireActivity() as EditCaseActivity).viewModel
        binding.viewmodel = viewModel

        viewModel.getCategories()
        initObservers()
    }

    private fun initObservers() {
        viewModel.currentCaseModel.observe(viewLifecycleOwner) { caseModel ->

            caseModel.mainImage?.let {
                viewModel.setImageSelected(true)
                viewModel.imageUploaded = true
                showMainImage(false, imageUrl = it)
            } ?: run {
                showMainImagePlaceHolder()
            }

            caseModel.title?.let {
                binding.caseTitleInput.setText(it)
            }

            caseModel.description?.let {
                binding.caseDescriptionInput.setText(it)
            }

            caseModel.address?.let {
                binding.caseManualAddressInput.setText(it)
            }

            caseModel.locationName?.let {
                binding.caseLocationInput.setText(it)
            }
        }

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
                    localeImageSelected(fileUri)
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

    private fun localeImageSelected(fileUri: Uri) {
        showMainImage(true, fileUri = fileUri)
    }

    fun showMainImagePlaceHolder() {
        binding.clearProfilePic.visibility = View.GONE
        binding.mainImageView.visibility = View.GONE
        binding.mainImagePlaceholderView.visibility = View.VISIBLE
    }

    fun showMainImage(isLocale: Boolean, fileUri: Uri? = null, imageUrl: String? = null) {
        binding.clearProfilePic.visibility = View.VISIBLE
        binding.mainImageView.visibility = View.VISIBLE
        binding.mainImagePlaceholderView.visibility = View.GONE

        if (isLocale) {
            fileUri?.let {
                binding.mainImageView.setImageURI(fileUri)
                viewModel.setProfileImageLocalPath(fileUri.path!!)
            }
        } else {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.mainImageView)
        }
    }

    companion object {
        private const val TAG = "CaseDetailsFragment"
    }
}