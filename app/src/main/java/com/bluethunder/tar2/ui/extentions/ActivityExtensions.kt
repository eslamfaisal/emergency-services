package com.bluethunder.tar2.ui.extentions

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.bluethunder.tar2.ui.ViewModelFactory
import kotlin.math.roundToInt

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

fun Activity.addKeyboardToggleListener(onKeyboardToggleAction: (shown: Boolean) -> Unit): KeyboardToggleListener? {
    val root = findViewById<View>(android.R.id.content)
    val listener = KeyboardToggleListener(root, onKeyboardToggleAction)
    return root?.viewTreeObserver?.run {
        addOnGlobalLayoutListener(listener)
        listener
    }
}

open class KeyboardToggleListener(
    private val root: View?,
    private val onKeyboardToggleAction: (shown: Boolean) -> Unit
) : ViewTreeObserver.OnGlobalLayoutListener {
    private var shown = false
    override fun onGlobalLayout() {
        root?.run {
            val heightDiff = rootView.height - height
            val keyboardShown = heightDiff > dpToPx(200f)
            if (shown != keyboardShown) {
                onKeyboardToggleAction.invoke(keyboardShown)
                shown = keyboardShown
            }
        }
    }
}

fun View.dpToPx(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).roundToInt()

fun hideKeyboard(activity: Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}