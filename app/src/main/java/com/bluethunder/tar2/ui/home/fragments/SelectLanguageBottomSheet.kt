package com.bluethunder.tar2.ui.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.SelectLanguageBottomSheetBinding
import com.bluethunder.tar2.ui.splash.SplashActivity
import com.bluethunder.tar2.utils.SharedHelper
import com.bluethunder.tar2.utils.SharedHelperKeys
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SelectLanguageBottomSheet : BottomSheetDialogFragment() {


    private lateinit var binding: SelectLanguageBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.select_language_bottom_sheet,
                container,
                false
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    var oldLanguage = "en"
    private fun initViews() {
        oldLanguage = SharedHelper.getString(
            requireActivity(),
            SharedHelperKeys.LANGUAGE_KEY,
            defaultValue = "en"
        )!!
        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.arabicView.setOnClickListener {
            setSelectedLanguage(it.tag.toString())
        }
        binding.englishView.setOnClickListener {
            setSelectedLanguage(it.tag.toString())
        }
    }

    private fun setSelectedLanguage(language: String) {
        SharedHelper.putString(requireActivity(), SharedHelperKeys.LANGUAGE_KEY, language)
        val intent = Intent(requireActivity(), SplashActivity::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()

    }

}
