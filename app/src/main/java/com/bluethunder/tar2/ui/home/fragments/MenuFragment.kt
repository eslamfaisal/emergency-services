package com.bluethunder.tar2.ui.home.fragments

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Html
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
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.huawei.agconnect.auth.AGConnectAuth

class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding

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
        binding.usernameTv.text = SessionConstants.currentLoggedInUserModel!!.name
        val circularProgressDrawable = CircularProgressDrawable(requireActivity())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(this)
            .asBitmap()
            .load(SessionConstants.currentLoggedInUserModel!!.imageUrl)
            .optionalTransform(CircleCrop())
            .placeholder(circularProgressDrawable)
            .apply(RequestOptions().override(200, 200))
            .into(binding.profileImage)


        binding.logoutTv.paintFlags = binding.logoutTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.logoutTv.setOnClickListener {
            SessionConstants.currentLoggedInUserModel = null
            AGConnectAuth.getInstance().signOut()
            SharedHelper.putBoolean(requireActivity(), SharedHelperKeys.IS_LOGGED_IN, false)
            requireActivity().startActivity(Intent(requireActivity(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    companion object {
        private const val TAG = "MenuFragment"
    }
}