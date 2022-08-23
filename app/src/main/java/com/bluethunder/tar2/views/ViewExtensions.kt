package com.bluethunder.tar2.views

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R

fun Activity.setupRefreshLayout(
    refreshLayout: ScrollChildSwipeRefreshLayout,
    scrollUpChild: View? = null
) {
    refreshLayout.setColorSchemeColors(
        ContextCompat.getColor(this, R.color.colorPrimary),
        ContextCompat.getColor(this, R.color.colorAccent),
        ContextCompat.getColor(this, R.color.colorPrimaryDark)
    )
    // Set the scrolling view in the custom SwipeRefreshLayout.
    scrollUpChild?.let {
        refreshLayout.scrollUpChild = it
    }
}


fun setMarginsToView(v: View, l: Int, t: Int, r: Int, b: Int) {
    if (v.layoutParams is ViewGroup.MarginLayoutParams) {
        val p = v.layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(l, t, r, b)
        v.requestLayout()
    }
}