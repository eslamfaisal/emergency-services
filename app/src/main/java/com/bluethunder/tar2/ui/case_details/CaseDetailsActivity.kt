package com.bluethunder.tar2.ui.case_details

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.databinding.ActivityCaseDetailsBinding
import com.bluethunder.tar2.model.Status.SUCCESS
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.case_details.adapter.CommentsAdapter
import com.bluethunder.tar2.ui.case_details.model.CommentModel
import com.bluethunder.tar2.ui.case_details.model.CommentType
import com.bluethunder.tar2.ui.case_details.viewmodel.CaseDetailsViewModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.addKeyboardToggleListener
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.hideKeyboard
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.bluethunder.tar2.utils.TimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.firestore.FirebaseFirestore
import com.huawei.hms.maps.common.util.DistanceCalculator
import java.util.*


class CaseDetailsActivity : AppCompatActivity() {

    private val viewModel by viewModels<CaseDetailsViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityCaseDetailsBinding

    lateinit var currentCase: CaseModel
    lateinit var currentCaseUser: CaseModel
    var currentCaseUserDetails: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaseDetailsBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)
        currentCase = intent.getSerializableExtra(CasesListFragment.CASE_LIST) as CaseModel

        initViews()
        initViewModel()
    }

    fun initViews() {
        setUpCaseDetails()
        setUpListeners()
        initCommentsView()
    }

    private fun setUpListeners() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.sendCommentIv.setOnClickListener {
            hideKeyboard(this)
            sendComments()
        }
        binding.upVotesView.setOnClickListener {
            sendUpVote()
        }
        binding.locationDirectionView.setOnClickListener {
            tryOpenLocationOnMap()
        }
        addKeyboardToggleListener { isShow ->
            Log.d(TAG, "addKeyboardToggleListener: $isShow")
            if (isShow) {
                binding.footerView.visibility = View.GONE
                binding.sendCommentIv.visibility = View.VISIBLE
            } else {
                binding.footerView.visibility = View.VISIBLE
                binding.sendCommentIv.visibility = View.GONE
            }
        }
    }

    private fun setUpCaseDetails() {
        setMainImage()
        currentCase.createdAt.let {
            val timeAgo = TimeAgo()
            timeAgo.locale(binding.root.context)
            binding.dateTv.text = timeAgo.getTimeAgo(it)
        }
        currentCase.userName?.let { binding.usernameTv.text = it }
        currentCase.title?.let { binding.caseTitleTv.text = it }
        currentCase.upVotesCount.let { binding.upVotesTv.text = it.toString() }
        currentCase.viewsCount.let {
            binding.viewsTv.text = it.toString()
        }
        currentCase.commentsCount.let {
            binding.commentsTv.text = it.toString()
        }
        currentCase.description.let {
            binding.caseDescriptionTv.text = it.toString()
        }
    }

    private fun sendUpVote() {
        viewModel.sendUpVote(currentCase.id!!)
    }

    private fun tryOpenLocationOnMap() {
        try {
            val uri = Uri.parse("geo:0,0?q=${currentCase.latitude},${currentCase.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.maps_not_found_msg),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sendComments() {
        val commentModel = CommentModel()
        commentModel.id = FirebaseFirestore.getInstance()
            .collection("cases").document().id
        commentModel.comment = binding.commentsEt.text.toString()
        commentModel.createdAt = Date()
        commentModel.type = CommentType.Text.name
        commentModel.caseId = currentCase.id
        commentModel.userImage = SessionConstants.currentLoggedInUserModel!!.imageUrl
        commentModel.userId = SessionConstants.currentLoggedInUserModel!!.id
        commentModel.userName = SessionConstants.currentLoggedInUserModel!!.name
        viewModel.sendComment(currentCase.id!!, commentModel)
        binding.commentsEt.setText("")
    }

    private fun initViewModel() {
        viewModel.getCaseCategory(currentCase.categoryId!!)
        viewModel.listenToComments(currentCase.id!!)
        viewModel.listenToCaseDetails(currentCase.id!!)
        viewModel.listenToCaseUserDetails(currentCase.userId!!)

        viewModel.commentsList.observe(this) { resources ->
            when (resources.status) {
                SUCCESS -> {
                    binding.commentsProgressBar.visibility = View.GONE
                    commentsAdapter.addNewData(resources.data!!)
                }
                else -> {
                    binding.commentsProgressBar.visibility = View.GONE
                }
            }
        }
        viewModel.caseCategory.observe(this) { category ->
            category?.let {
                binding.categoryTv.text =
                    if (SessionConstants.currentLanguage == "ar") it.nameAr else it.nameEn
            }
        }
        viewModel.currentCaseDetails.observe(this) { currentCase ->
            currentCase?.let {
                this.currentCase = it
                setUpCaseDetails()
            }
        }

        viewModel.currentCaseUserDetails.observe(this) { currentCase ->
            currentCase?.let {
                this.currentCaseUserDetails = it
                currentCaseUserDetails?.let {
                    setUpCaseUserDetails()
                }
            }
        }

    }

    private fun setUpCaseUserDetails() {
        setUserImage()
        currentCase.userName?.let { binding.usernameTv.text = it }
        currentCaseUserDetails?.let { userDetails ->
            binding.phoneCallBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse(
                    "tel:+${userDetails.phone.trim()}"
                )
                startActivity(intent)
            }
        }

    }

    lateinit var commentsAdapter: CommentsAdapter
    private fun initCommentsView() {
        commentsAdapter = CommentsAdapter()
        binding.commentsRecyclerView.apply {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(this@CaseDetailsActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@CaseDetailsActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
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

    }

    private fun setUserImage() {

        val circularProgressDrawable =
            CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(this)
            .load(currentCase.userImage)
            .placeholder(circularProgressDrawable)
            .optionalTransform(CircleCrop())
            .error(R.drawable.ic_small_profile_image_place_holder)
            .into(binding.profileImage)

    }

    fun calculateDistance(){
//        DistanceCalculator.computeDistanceBetween()
        val startPoint = Location("locationA")
        startPoint.setLatitude(17.372102)
        startPoint.setLongitude(78.484196)

        val endPoint = Location("locationA")
        endPoint.setLatitude(17.375775)
        endPoint.setLongitude(78.469218)

        val distance: Float = startPoint.distanceTo(endPoint)
    }
    companion object {
        private const val TAG = "CaseDetailsActivity"
    }
}