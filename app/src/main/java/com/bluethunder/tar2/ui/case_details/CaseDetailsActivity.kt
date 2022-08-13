package com.bluethunder.tar2.ui.case_details

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.ActivityCaseDetailsBinding
import com.bluethunder.tar2.databinding.ActivityMainBinding
import com.bluethunder.tar2.ui.case_details.viewmodel.CaseDetailsViewModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.setTransparentStatusBar
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.bluethunder.tar2.ui.home.viewmodel.HomeViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class CaseDetailsActivity : AppCompatActivity() {

    private val viewModel by viewModels<CaseDetailsViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityCaseDetailsBinding

    lateinit var currentCase: CaseModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaseDetailsBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)
        currentCase = intent.getSerializableExtra(CasesListFragment.CASE_LIST) as CaseModel

        initVideos()
//        setTransparentStatusBar()
    }

    fun initVideos() {
        setMainImage()
    }

    private fun setMainImage() {

        val circularProgressDrawable =
            CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(this)
            .load(currentCase.mainImage)
            .placeholder(circularProgressDrawable)

            .placeholder(circularProgressDrawable)
            .into(binding.mainImage)

    }
}