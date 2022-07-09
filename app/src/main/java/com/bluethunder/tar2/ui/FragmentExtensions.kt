package com.bluethunder.tar2.ui

import androidx.fragment.app.Fragment

fun Fragment.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(requireActivity(), this)
}
