package com.bluethunder.tar2.ui.extentions

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.ui.ViewModelFactory

fun AppCompatActivity.getViewModelFactory(): ViewModelFactory {
    return ViewModelFactory(this, this)
}

fun Activity.setTransparentStatusBar() {
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = Color.TRANSPARENT
    }
}