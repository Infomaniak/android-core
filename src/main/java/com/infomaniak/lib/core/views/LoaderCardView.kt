/*
 * Copyright (C) 2023 Infomaniak Network SA
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
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.card.MaterialCardView
import com.infomaniak.lib.core.R

class LoaderCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr), LoaderView {
    private var loaderController: LoaderController = LoaderController(this)
    private var defaultColorResource = ContextCompat.getColor(context, R.color.loaderDefault)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        loaderController.onDraw(canvas)
    }

    private fun setChildrenVisibility(isVisible: Boolean) {
        for (view in children) {
            view.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }
    }

    fun startLoading() {
        loaderController.startLoading()
    }

    fun stopLoading() {
        loaderController.stopLoading()
    }

    fun start() {
        startLoading()
        setChildrenVisibility(false)
    }

    fun stop() {
        stopLoading()
        setChildrenVisibility(true)
    }

    override fun setRectColor(rectPaint: Paint) {
        rectPaint.color = defaultColorResource
    }

    override fun valueSet(): Boolean {
        return false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loaderController.removeAnimatorUpdateListener()
    }
}
