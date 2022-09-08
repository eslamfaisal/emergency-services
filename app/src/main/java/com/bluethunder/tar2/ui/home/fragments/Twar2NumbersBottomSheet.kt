package com.bluethunder.tar2.ui.home.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bluethunder.tar2.R
import com.bluethunder.tar2.databinding.Twar2NumbersBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class Twar2NumbersBottomSheet : BottomSheetDialogFragment() {


    private lateinit var binding: Twar2NumbersBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style. AppBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.twar2_numbers_bottom_sheet, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       initViews()
    }

    private fun initViews() {
        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.policeNumber.setOnClickListener {
           goToDial("122")
        }
        binding.ambulanceNumber.setOnClickListener {
           goToDial("123")
        }
        binding.hotLinesSpecial.setOnClickListener {
           goToDial("15044")
        }
        binding.babyHelp.setOnClickListener {
           goToDial("16000")
        }
    }

    private fun goToDial(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)

    }

}
