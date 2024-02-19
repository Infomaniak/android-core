/*
 * Copyright 2016 Elye Project
 * Copyright (C) 2022-2023 Infomaniak Network SA
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
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.view.animation.LinearInterpolator

internal class LoaderController(private val loaderView: LoaderView) : AnimatorUpdateListener {

    private lateinit var rectPaint: Paint
    private var linearGradient: LinearGradient? = null
    private var progress = 0.0f
    private lateinit var valueAnimator: ValueAnimator

    private var widthWeight = LoaderConstant.MAX_WEIGHT
    private var heightWeight = LoaderConstant.MAX_WEIGHT
    private var useGradient = LoaderConstant.USE_GRADIENT_DEFAULT
    var corners = LoaderConstant.CORNER_DEFAULT

    init {
        init()
    }

    private fun init() {
        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        loaderView.setRectColor(rectPaint)
        setValueAnimator(0.5f, 1.0f, ObjectAnimator.INFINITE)
    }

    fun onDraw(
        canvas: Canvas,
        leftPad: Float = 0.0f,
        topPad: Float = 0.0f,
        rightPad: Float = 0.0f,
        bottomPad: Float = 0.0f,
    ) {
        val marginHeight = canvas.height * (1.0f - heightWeight) / 2.0f
        rectPaint.alpha = (progress * MAX_COLOR_CONSTANT_VALUE).toInt()
        if (useGradient) prepareGradient(canvas.width * widthWeight)

        canvas.drawRoundRect(
            RectF(
                0.0f + leftPad,
                marginHeight + topPad,
                canvas.width * widthWeight - rightPad,
                canvas.height - marginHeight - bottomPad,
            ),
            corners.toFloat(), corners.toFloat(),
            rectPaint,
        )
    }

    private fun prepareGradient(width: Float) {
        if (linearGradient == null) {
            linearGradient = LinearGradient(
                0.0f,
                0.0f,
                width,
                0.0f,
                rectPaint.color,
                LoaderConstant.COLOR_DEFAULT_GRADIENT,
                TileMode.MIRROR,
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

    fun stopLoading() {
        valueAnimator.cancel()
        setValueAnimator(progress, 0.0f, 0)
        valueAnimator.start()
    }

    private fun setValueAnimator(begin: Float, end: Float, newRepeatCount: Int) {
        valueAnimator = ValueAnimator.ofFloat(begin, end).apply {
            repeatCount = newRepeatCount
            duration = ANIMATION_CYCLE_DURATION.toLong()
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener(this@LoaderController)
        }
    }

    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        progress = valueAnimator.animatedValue as Float
        loaderView.invalidate()
    }

    fun removeAnimatorUpdateListener() {
        valueAnimator.removeUpdateListener(this)
        valueAnimator.cancel()
        progress = 0.0f
    }

    private companion object {
        const val MAX_COLOR_CONSTANT_VALUE = 255
        const val ANIMATION_CYCLE_DURATION = 750 // In milliseconds
    }
}
