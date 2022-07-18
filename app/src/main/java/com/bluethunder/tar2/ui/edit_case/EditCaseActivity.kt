package com.bluethunder.tar2.ui.edit_case

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.databinding.ActivityEditCaseBinding
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory

class EditCaseActivity : AppCompatActivity() {

    private val viewModel by viewModels<EditCaseViewModel> { getViewModelFactory() }

    private lateinit var binding: ActivityEditCaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCaseBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        initViews()
        initViewModel()
    }

    private fun initViews() {

    }

    private fun initViewModel() {

    }

    companion object {
        const val EXTRA_IS_NEW_CASE = "com.bluethunder.tar2.ui.edit_case.EXTRA_IS_NEW_CASE"
    }
}