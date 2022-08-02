package com.bluethunder.tar2.views

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bluethunder.tar2.R

fun Fragment.setupRefreshLayout(
    refreshLayout: ScrollChildSwipeRefreshLayout,
    scrollUpChild: View? = null
) {
    refreshLayout.setColorSchemeColors(
        ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
        ContextCompat.getColor(requireActivity(), R.color.colorAccent),
        ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark)
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