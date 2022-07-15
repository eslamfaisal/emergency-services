package com.bluethunder.tar2.ui.extentions

import androidx.fragment.app.Fragment
import com.bluethunder.tar2.ui.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(requireActivity(), this)
}
