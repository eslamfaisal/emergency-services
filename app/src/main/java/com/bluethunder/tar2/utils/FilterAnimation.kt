package com.bluethunder.tar2.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class FilterAnimation(
    private val view: View,
    private val toHeight: Float,
    private val fromHeight: Float,
    private val toWidth: Float,
    private val fromWidth: Float,
    duration: Long
) : Animation() {

    init {
        this.duration = duration
    }

    override fun applyTransformation(
        interpolatedTime: Float,
        t: Transformation?
    ) {
        val height = (toHeight - fromHeight) * interpolatedTime + fromHeight
        val width = (toWidth - fromWidth) * interpolatedTime + fromWidth
        val layoutParams = view.layoutParams
        layoutParams.height = height.toInt()
        layoutParams.width = width.toInt()
        view.requestLayout()
    }
}

class ResizeAnimation : Animation {
    private var mView: View
    private var mToHeight: Float
    private var mFromHeight: Float
    private var mToWidth: Float
    private var mFromWidth: Float

    constructor(v: View, fromWidth: Float, fromHeight: Float, toWidth: Float, toHeight: Float) {
        mToHeight = toHeight
        mToWidth = toWidth
        mFromHeight = fromHeight
        mFromWidth = fromWidth
        mView = v
        duration = 200
    }

    constructor(
        v: View,
        toHeight: Float,
        fromHeight: Float,
        toWidth: Float,
        fromWidth: Float,
        duration: Int
    ) {
        mToHeight = toHeight
        mToWidth = toWidth
        mFromHeight = fromHeight
        mFromWidth = fromWidth
        mView = v
        setDuration(duration.toLong())
    }

    constructor(
        v: View,
        fromWidth: Float,
        fromHeight: Float,
        toWidth: Float,
        toHeight: Float,
        duration: Int,
        offset: Int
    ) {
        mToHeight = toHeight
        mToWidth = toWidth
        mFromHeight = fromHeight
        mFromWidth = fromWidth
        mView = v
        setDuration(duration.toLong())
        startOffset = offset.toLong()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight
        val width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth
        val p = mView.layoutParams
        p.height = height.toInt()
        p.width = width.toInt()
        mView.requestLayout()
    }
}