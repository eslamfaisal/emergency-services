package com.bluethunder.tar2.ui

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R

fun Fragment.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(requireActivity(), this)
}

fun Context.showLoadingDialog(): Dialog {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
    dialog.setContentView(R.layout.loading_layout)
    dialog.setCancelable(true)
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent);
    dialog.window!!.attributes = lp
    return dialog
}
