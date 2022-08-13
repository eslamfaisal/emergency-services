package com.bluethunder.tar2.ui.case_details

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.databinding.ActivityCaseDetailsBinding
import com.bluethunder.tar2.ui.case_details.viewmodel.CaseDetailsViewModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.addKeyboardToggleListener
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.bluethunder.tar2.utils.TimeAgo
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
        getCaseCategory()
        initViews()
//        setTransparentStatusBar()
    }

    private fun getCaseCategory() {

    }

    fun initViews() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        setMainImage()
        currentCase.createdAt.let {
            val timeAgo = TimeAgo()
            timeAgo.locale(binding.root.context)
            binding.dateTv.text = timeAgo.getTimeAgo(it)
        }
        currentCase.userName?.let { binding.usernameTv.text = it }
        currentCase.title?.let { binding.caseTitleTv.text = it }

        currentCase.viewsCount.let {
            binding.viewsTv.text = it.toString()
        }
        currentCase.commentsCount.let {
            binding.commentsTv.text = it.toString()
        }

        currentCase.description.let {
            binding.caseDescriptionTv.text = it.toString()
        }

        addKeyboardToggleListener { isShow ->
            Log.d(TAG, "addKeyboardToggleListener: $isShow")
            if (isShow) {
            } else {

            }
        }
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
            .into(binding.mainImage)

        Glide.with(this)
            .load(currentCase.userImage)
            .placeholder(circularProgressDrawable)
            .optionalTransform(CircleCrop())
            .into(binding.profileImage)

    }

    companion object {
        private const val TAG = "CaseDetailsActivity"
    }
}