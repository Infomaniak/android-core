/*
 * Copyright 2016 Elye Project
 * Copyright (C) 2022 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.infomaniak.lib.core.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.*
import android.view.animation.LinearInterpolator
import kotlin.math.max

internal class LoaderController(private val loaderView: LoaderView) : AnimatorUpdateListener {
    private lateinit var rectPaint: Paint
    private var linearGradient: LinearGradient? = null
    private var progress = 0f
    private lateinit var valueAnimator: ValueAnimator
    var widthWeight = LoaderConstant.MAX_WEIGHT
    var heightWeight = LoaderConstant.MAX_WEIGHT
    var useGradient = LoaderConstant.USE_GRADIENT_DEFAULT
    var corners = LoaderConstant.CORNER_DEFAULT

    companion object {
        private const val MAX_COLOR_CONSTANT_VALUE = 255
        private const val ANIMATION_CYCLE_DURATION = 750 //milis
    }

    init {
        init()
    }

    private fun init() {
        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        loaderView.setRectColor(rectPaint)
        setValueAnimator(0.5f, 1f, ObjectAnimator.INFINITE)
    }

    fun onDraw(
        canvas: Canvas,
        left_pad: Float = 0f,
        top_pad: Float = 0f,
        right_pad: Float = 0f,
        bottom_pad: Float = 0f
    ) {
        val marginHeight = canvas.height * (1 - heightWeight) / 2
        rectPaint.alpha = (progress * MAX_COLOR_CONSTANT_VALUE).toInt()
        if (useGradient) {
            prepareGradient(canvas.width * widthWeight)
        }
        canvas.drawRoundRect(
            RectF(
                0 + left_pad,
                marginHeight + top_pad,
                canvas.width * widthWeight - right_pad,
                canvas.height - marginHeight - bottom_pad
            ),
            corners.toFloat(), corners.toFloat(),
            rectPaint
        )
    }

    private fun prepareGradient(width: Float) {
        if (linearGradient == null) {
            linearGradient = LinearGradient(
                0f, 0f, width, 0f, rectPaint.color,
                LoaderConstant.COLOR_DEFAULT_GRADIENT, Shader.TileMode.MIRROR
            )
        }
        rectPaint.shader = linearGradient
    }

    fun onSizeChanged() {
        linearGradient = null
        startLoading()
    }

    fun startLoading() {
        if (!loaderView.valueSet()) {
            valueAnimator.cancel()
            init()
            valueAnimator.start()
        }
    }

    private fun validateWeight(weight: Float): Float {
        return if (weight > LoaderConstant.MAX_WEIGHT) LoaderConstant.MAX_WEIGHT else max(weight, LoaderConstant.MIN_WEIGHT)
    }

    fun stopLoading() {
        valueAnimator.cancel()
        setValueAnimator(progress, 0f, 0)
        valueAnimator.start()
    }

    private fun setValueAnimator(begin: Float, end: Float, repeatCount: Int) {
        valueAnimator = ValueAnimator.ofFloat(begin, end)
        valueAnimator.repeatCount = repeatCount
        valueAnimator.duration = ANIMATION_CYCLE_DURATION.toLong()
        valueAnimator.repeatMode = ValueAnimator.REVERSE
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener(this)
    }

    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        progress = valueAnimator.animatedValue as Float
        loaderView.invalidate()
    }

    fun removeAnimatorUpdateListener() {
        valueAnimator.removeUpdateListener(this)
        valueAnimator.cancel()
        progress = 0f
    }
}