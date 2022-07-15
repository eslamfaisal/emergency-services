package com.bluethunder.tar2.ui.extentions

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bluethunder.tar2.R
import com.google.android.material.snackbar.Snackbar


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


fun View.showSnakeBarError(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)
        .setAction(this.context.resources.getString(R.string.ok)) {}
        .setActionTextColor(
            ContextCompat.getColor(this.context, R.color.colorWhite)
        ).show()
}