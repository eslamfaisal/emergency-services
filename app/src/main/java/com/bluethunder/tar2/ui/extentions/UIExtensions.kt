package com.bluethunder.tar2.ui.extentions

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.bluethunder.tar2.R
import com.bluethunder.tar2.ui.auth.AuthActivity
import com.google.android.material.snackbar.Snackbar
import java.util.*


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

fun setAppLocale(activity: Activity, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val resources: Resources = activity.resources
    val config: Configuration = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
    val intent = Intent(activity, AuthActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    activity.startActivity(intent)
    activity.finish()
}