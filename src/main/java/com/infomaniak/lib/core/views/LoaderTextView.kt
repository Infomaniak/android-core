/*
 * Infomaniak Core - Android
 * Copyright 2016 Elye Project
 * Copyright (C) 2022-2024 Infomaniak Network SA
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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View.MeasureSpec.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.infomaniak.lib.core.R
import com.infomaniak.lib.core.utils.getAttributes
import kotlin.math.min

class LoaderTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr), LoaderView {

    private var loaderController: LoaderController = LoaderController(this)
    private var defaultColorResource = ContextCompat.getColor(context, R.color.loaderDefault)

    init {
        attrs?.getAttributes(context, R.styleable.LoaderTextView) {
            if (getBoolean(R.styleable.LoaderTextView_loader_useDarkColor, false)) {
                defaultColorResource = ContextCompat.getColor(context, R.color.loaderDarkerDefault)
            }
            loaderController.corners = getDimensionPixelSize(
                R.styleable.LoaderTextView_loader_corner,
                LoaderConstant.CORNER_DEFAULT,
            )
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        loaderController.onSizeChanged()
    }

    fun resetLoader() {
        text = ""
        loaderController.startLoading()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = getSize(widthMeasureSpec)
        val widthMeasureMode = getMode(widthMeasureSpec)
        val maxWidthMeasureSpec = makeMeasureSpec(min(maxWidth, widthSize), widthMeasureMode)
        super.onMeasure(maxWidthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        loaderController.onDraw(
            canvas,
            compoundPaddingLeft.toFloat(),
            compoundPaddingTop.toFloat(),
            compoundPaddingRight.toFloat(),
            compoundPaddingBottom.toFloat(),
        )
    }

    override fun setText(text: CharSequence, type: BufferType) {
        super.setText(text, type)
        if (text.isNotBlank()) loaderController.stopLoading()
    }

    override fun setRectColor(rectPaint: Paint) {
        rectPaint.color = defaultColorResource
    }

    override fun valueSet(): Boolean = text.isNotEmpty()

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loaderController.removeAnimatorUpdateListener()
    }
}
