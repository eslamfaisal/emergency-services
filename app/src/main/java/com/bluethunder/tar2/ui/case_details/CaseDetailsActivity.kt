package com.bluethunder.tar2.ui.case_details

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.SessionConstants.myCurrentLocation
import com.bluethunder.tar2.databinding.ActivityCaseDetailsBinding
import com.bluethunder.tar2.model.Status.*
import com.bluethunder.tar2.ui.MyLocationViewModel
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.case_details.adapter.CommentsAdapter
import com.bluethunder.tar2.ui.case_details.model.CommentModel
import com.bluethunder.tar2.ui.case_details.model.CommentType
import com.bluethunder.tar2.ui.case_details.viewmodel.CaseDetailsViewModel
import com.bluethunder.tar2.ui.edit_case.EditCaseActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.extentions.addKeyboardToggleListener
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.extentions.hideKeyboard
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.bluethunder.tar2.ui.home.model.CaseStatus
import com.bluethunder.tar2.utils.TimeAgo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.firestore.FirebaseFirestore
import com.huawei.agconnect.applinking.AppLinking
import com.huawei.agconnect.applinking.ShortAppLinking
import com.huawei.hms.maps.common.util.DistanceCalculator
import com.huawei.hms.maps.model.LatLng
import java.lang.reflect.Field
import java.util.*


class CaseDetailsActivity : AppCompatActivity() {

    private val myLocationViewModel by viewModels<MyLocationViewModel> { getViewModelFactory() }
    private val viewModel by viewModels<CaseDetailsViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityCaseDetailsBinding
    private lateinit var progressDialog: Dialog

    lateinit var currentCase: CaseModel
    var myCase = false
    var currentCaseUserDetails: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaseDetailsBinding.inflate(layoutInflater)
        binding.viewmodel = viewModel
        setContentView(binding.root)

        getExtraData()

        initViews()
        initViewModel()
    }

    private fun getExtraData() {
        currentCase = intent.getSerializableExtra(CasesListFragment.EXTRA_CASE_MODEL) as CaseModel
        myCase = currentCase.userId == SessionConstants.currentLoggedInUserModel!!.id
    }

    fun initViews() {
        progressDialog = showLoadingDialog()
        setUpCaseDetails()
        setUpListeners()
        initCommentsView()
        checkMenuBtn()
//        calculateDistance()
    }

    private fun setUpListeners() {
        binding.backBtn.setOnClickListener {
            finish()
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

    private fun checkMenuBtn() {
        if (myCase) {
            binding.menuImage.setImageDrawable(resources.getDrawable(R.drawable.ic_more))
        } else {
            binding.menuImage.setImageDrawable(resources.getDrawable(R.drawable.ic_share_case))
        }
        binding.menuBtn.setOnClickListener {
            if (myCase) {
                showMenuPopup(it)
            } else {
                createAppLinking()
            }
        }

    }

    private fun showMenuPopup(v: View) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(R.menu.case_menu, popup.menu)

        val menuHelper: Any
        val argTypes: Array<Class<*>?>
        try {
            val fMenuHelper: Field = PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper.isAccessible = true
            menuHelper = fMenuHelper.get(popup)
            argTypes = arrayOf(Boolean::class.javaPrimitiveType)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes)
                .invoke(menuHelper, true)
        } catch (e: java.lang.Exception) {
        }
        popup.setOnMenuItemClickListener { item ->
            when (item!!.itemId) {
                R.id.share_case -> {
                    createAppLinking()
                }
                R.id.edit_case -> {
                    editCase()
                }
            }
            true
        }

        popup.show()
    }

    private fun editCase() {
        val intent = Intent(this, EditCaseActivity::class.java)
        intent.putExtra(EditCaseActivity.EXTRA_IS_NEW_CASE, false)
        intent.putExtra(EditCaseActivity.EXTRA_CASE, currentCase)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setUpCaseDetails() {
        if (myCase) {
            binding.caseStatusView.visibility = View.VISIBLE
            binding.caseActionsView.visibility = View.GONE
            setupCaseStatus()
        } else {
            binding.caseActionsView.visibility = View.VISIBLE
            binding.caseUserView.visibility = View.GONE
            binding.caseStatusView.visibility = View.GONE
        }
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

    private fun setupCaseStatus() {
        when (currentCase.status) {
            CaseStatus.Published.name -> {
                binding.caseStatusName.text = getString(R.string.published)
                binding.caseStatusName.setTextColor(resources.getColor(R.color.color_published))
            }
            CaseStatus.Saved.name -> {
                binding.caseStatusName.text = getString(R.string.saved)
                binding.caseStatusName.setTextColor(resources.getColor(R.color.greenDarkColor))
            }
            CaseStatus.UnPublished.name -> {
                binding.caseStatusName.text = getString(R.string.un_published)
                binding.caseStatusName.setTextColor(resources.getColor(R.color.color_unpublished))
            }
        }
        binding.caseStatusView.setOnClickListener {
            showCaseStatusBottomSheet()
        }
    }

    private fun showCaseStatusBottomSheet() {

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
            showDownloadMapDialog()
        }
    }

    private fun showDownloadMapDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.maps_not_found_msg))
        builder.setPositiveButton(getString(R.string.downlad_map_app)) { dialog, which ->
            startHuaweiAppGallery()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.open_on_web)) { dialog, which ->
            openWebPage()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun startHuaweiAppGallery() {
       val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://appgallery.huawei.com/app/C102457337")
        )
        startActivity(intent)
    }

    fun openWebPage() {
        val url =
            "https://www.google.com/maps/search/?api=1&query=${currentCase.latitude},${currentCase.longitude}"
        val builder = CustomTabsIntent.Builder()
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(resources.getColor(R.color.colorPrimary))
            .build()
        builder.setDefaultColorSchemeParams(defaultColors)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
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

        if (!myCase)
            viewModel.listenToCaseUserDetails(currentCase.userId!!)

        calculateDistance()

        viewModel.caseLocationDistance.observe(this) { resources ->
            when (resources.status) {
                SUCCESS -> {
                    binding.distanceProgressBar.visibility = View.GONE
                    try {
                        resources.data?.let {
                            binding.distanceTv.text =
                                it.routes[0].paths[0].distanceText!!.toString().capitalized()
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "initViewModel: ${e.message}")
                    }
                }
                ERROR -> {
                    binding.caseDistanceView.visibility = View.GONE
                }
                LOADING -> {
                    binding.distanceProgressBar.visibility = View.VISIBLE
                }
                else -> {
                    binding.distanceProgressBar.visibility = View.GONE
                }
            }


        }

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

    private fun calculateDistance() {
        myCurrentLocation?.let {
            viewModel.getCaseLocationDistance(
                currentCase.latitude!!.toDouble(),
                currentCase.longitude!!.toDouble()
            )
        } ?: kotlin.run {
            myLocationViewModel.checkDeviceLocation(this, oneTimeRequest = true)
            myLocationViewModel.lastLocation.observe(this) { locationResource ->
                locationResource?.let {
                    it.data?.let { location ->
                        myCurrentLocation = LatLng(location.latitude, location.longitude)
                        Log.d(TAG, "initViewModel:myCurrentLocation = $myCurrentLocation")
                        viewModel.getCaseLocationDistance(
                            currentCase.latitude!!.toDouble(),
                            currentCase.longitude!!.toDouble()
                        )
                    }
                }
            }
        }
    }

    fun String.capitalized(): String {
        return this.uppercase(Locale.getDefault())
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

    fun calculateFixedDistance() {
        myCurrentLocation?.let {
            val distance = DistanceCalculator.computeDistanceBetween(
                it,
                LatLng(
                    currentCase.latitude!!.toDouble(),
                    currentCase.longitude!!.toDouble()
                )
            )

            val number2digits: Double = String.format("%.2f", distance / 1000).toDouble()
            Log.d(TAG, "calculateDistance: $distance")
            Log.d(TAG, "calculateDistance: $number2digits")
        }

    }

    private fun createAppLinking() {
        progressDialog.show()
        val builder = AppLinking.Builder()
            .setUriPrefix("https://tar2.dra.agconnect.link")
            .setDeepLink(Uri.parse("https://tar2.xyz/${currentCase.id}"))
            .setAndroidLinkInfo(AppLinking.AndroidLinkInfo.Builder().build())
            .setSocialCardInfo(
                AppLinking.SocialCardInfo.newBuilder()
                    .setTitle(currentCase.title)
                    .setDescription(currentCase.description)
                    .setImageUrl(currentCase.mainImage)
                    .build()
            )
            .setIsShowPreview(true)

        builder.buildShortAppLinking(ShortAppLinking.LENGTH.SHORT)
            .addOnSuccessListener { shortAppLinking: ShortAppLinking ->
                shareAppLink(shortAppLinking.shortUrl.toString())
                progressDialog.dismiss()
            }
            .addOnFailureListener { e: Exception ->
                showError(e.message)
                progressDialog.dismiss()
            }

    }

    private fun showError(msg: String?) {
        Log.d(TAG, "showError: $msg")
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun shareAppLink(shortUrl: String?) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shortUrl)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EditCaseActivity.REQUEST_DEVICE_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    calculateDistance()
                } else {
                    Log.i(TAG, "User denied request")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendView(currentCase.id!!)
    }

    companion object {
        private const val TAG = "CaseDetailsActivity"
    }
}