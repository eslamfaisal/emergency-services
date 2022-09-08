package com.bluethunder.tar2.ui.home.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.databinding.FragmentMenuBinding
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.bluethunder.tar2.ui.extentions.showLoadingDialog
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huawei.agconnect.auth.AGConnectAuth

class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding
    private lateinit var progressDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_menu, container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        progressDialog = requireActivity().showLoadingDialog()
        binding.usernameTv.text = SessionConstants.currentLoggedInUserModel!!.name
        SessionConstants.currentLoggedInUserModel!!.imageUrl?.let {
            setUserImage(it)
        }

        binding.logoutTv.paintFlags = binding.logoutTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.logoutTv.setOnClickListener {
            SessionConstants.currentLoggedInUserModel = null
            AGConnectAuth.getInstance().signOut()
            SharedHelper.putBoolean(requireActivity(), SharedHelperKeys.IS_LOGGED_IN, false)
            requireActivity().startActivity(Intent(requireActivity(), AuthActivity::class.java))
            requireActivity().finish()
        }

        binding.shareApp.setOnClickListener {
            shareAppLink("Download Tar2-طارق \n\nhttps://tar2.dra.agconnect.link/r-Tar2-App")
        }

        binding.privacyPolicy.setOnClickListener {
            val url = "https://github.com/eslamfaisal/tar2-privacy-policy#readme"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        binding.contactUs.setOnClickListener {
            contactUS()
        }
        binding.aboutApp.setOnClickListener {
            showAboutApp()
        }
        binding.twar2Numbers.setOnClickListener {
            openTwar2BottomSheet()
        }
    }

    private fun setUserImage(it: String) {
        val circularProgressDrawable = CircularProgressDrawable(requireActivity())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(this)
            .asBitmap()
            .load(it)
            .optionalTransform(CircleCrop())
            .placeholder(circularProgressDrawable)
            .apply(RequestOptions().override(200, 200))
            .into(binding.profileImage)
    }

    private fun openTwar2BottomSheet() {
        val sheet = Twar2NumbersBottomSheet()
        sheet.show(childFragmentManager, "numbers")
    }

    private fun showAboutApp() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(resources.getString(R.string.about_app))
            .setMessage(resources.getString(R.string.about_app_description))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun contactUS() {

        val content =
            "User ID = ${SessionConstants.currentLoggedInUserModel!!.id}\n\n" + "Please type your message below\n\n"

        val intent = Intent(Intent.ACTION_VIEW)
        val data = Uri.parse("mailto:eslamfaisal423@gmail.com?subject=Tar2 App&body=$content")
        intent.data = data
        startActivity(intent)

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

    companion object {
        private const val TAG = "MenuFragment"
    }
}